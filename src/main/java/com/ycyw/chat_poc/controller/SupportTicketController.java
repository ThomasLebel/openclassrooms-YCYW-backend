package com.ycyw.chat_poc.controller;

import com.ycyw.chat_poc.dto.BadgeUpdateDTO;
import com.ycyw.chat_poc.dto.NewSupportTicketPayload;
import com.ycyw.chat_poc.dto.SupportTicketDTO;
import com.ycyw.chat_poc.dto.TicketSummaryDTO;
import com.ycyw.chat_poc.models.ConversationStatus;
import com.ycyw.chat_poc.models.Message;
import com.ycyw.chat_poc.models.SupportTicket;
import com.ycyw.chat_poc.repository.MessageRepository;
import com.ycyw.chat_poc.repository.SupportTicketRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/supportTickets")
public class SupportTicketController {
    private final SupportTicketRepository supportTicketRepository;
    private final MessageRepository messageRepository;
    private final SimpMessagingTemplate messagingTemplate;

    public SupportTicketController(SupportTicketRepository supportTicketRepository,
                                   MessageRepository messageRepository,
                                   SimpMessagingTemplate messagingTemplate) {
        this.supportTicketRepository = supportTicketRepository;
        this.messageRepository = messageRepository;
        this.messagingTemplate = messagingTemplate;
    }

    @PostMapping
    public ResponseEntity<SupportTicketDTO> create(@RequestBody NewSupportTicketPayload payload) {
        SupportTicket supportTicket = new SupportTicket();
        supportTicket.setUserPseudo(payload.getUserPseudo());
        supportTicket.setSubject(payload.getSubject());
        supportTicket.setStatus(ConversationStatus.WAITING);
        supportTicketRepository.save(supportTicket);

        Message message = new Message();
        message.setSupportTicket(supportTicket);
        message.setIsFromAgent(false);
        message.setContent(payload.getMessage());
        messageRepository.save(message);

        // Envoi ticket dans le canal des tickets en attente pour les agents
        messagingTemplate.convertAndSend("/topic/supportTickets.waiting", supportTicket);

        SupportTicketDTO supportTicketDTO = new SupportTicketDTO(supportTicket.getId(), supportTicket.getSubject(), supportTicket.getCreatedAt());
        return ResponseEntity.ok(supportTicketDTO);
    }

    @GetMapping("/waiting")
    public List<SupportTicket> getWaitingSupportTickets() {
        return supportTicketRepository.findByStatusOrderByCreatedAtAsc(ConversationStatus.WAITING);
    }

    @PostMapping("/{id}/assign")
    public SupportTicket assign(@PathVariable Long id, @RequestParam String agentPseudo) {
        SupportTicket supportTicket = supportTicketRepository.findById(id).orElseThrow();

        if (supportTicket.getStatus() != ConversationStatus.WAITING) {
            throw new IllegalStateException("Conversation déjà assignée");
        }

        supportTicket.setAgentPseudo(agentPseudo);
        supportTicket.setStatus(ConversationStatus.ASSIGNED);
        supportTicketRepository.save(supportTicket);

        // Envoi de l'id du ticket assigné dans le canal pour les supprimer côté front
        messagingTemplate.convertAndSend("/topic/supportTickets.assigned", supportTicket);

        return supportTicket;
    }

    @GetMapping
    public List<TicketSummaryDTO> getHistory(@RequestParam String pseudo, @RequestParam boolean isFromAgent) {
        List<SupportTicket> supportTickets = isFromAgent
                ? supportTicketRepository.findByAgentPseudoOrderByUpdatedAtAsc(pseudo)
                : supportTicketRepository.findByUserPseudoOrderByUpdatedAtAsc(pseudo);
        return supportTickets.stream()
                .map(ticket -> new TicketSummaryDTO(
                        ticket.getId(),
                        isFromAgent ? ticket.getUserPseudo() : ticket.getAgentPseudo(),
                        ticket.getUpdatedAt(),
                        messageRepository.countUnread(ticket.getId(), !isFromAgent)

                )).toList();

    }

    @PostMapping("/{id}/messages")
    public List<Message> getMessages(@PathVariable Long id) {
        return messageRepository.findBySupportTicket_IdOrderBySentAtAsc(id);
    }

    @PostMapping("/{id}/read")
    public void markAsRead(@PathVariable Long id,
                           @RequestParam String readerPseudo,
                           @RequestParam boolean isAgent) {
        messageRepository.markAllAsRead(id, !isAgent);

        messagingTemplate.convertAndSend(
                "/topic/user." + readerPseudo + ".badge",
                new BadgeUpdateDTO(id, 0)
        );
    }

    // Total de messages non lus, à utiliser à la connexion pour la pastille globale
    @GetMapping("/badge/total")
    public int getTotalUnread(@RequestParam String pseudo, @RequestParam boolean isAgent) {
        return isAgent
                ? messageRepository.countTotalUnreadForAgent(pseudo)
                : messageRepository.countTotalUnreadForUser(pseudo);
    }
}
