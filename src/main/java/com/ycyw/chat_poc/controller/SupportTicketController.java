package com.ycyw.chat_poc.controller;

import com.ycyw.chat_poc.dto.*;
import com.ycyw.chat_poc.models.ConversationStatus;
import com.ycyw.chat_poc.models.Message;
import com.ycyw.chat_poc.models.SupportTicket;
import com.ycyw.chat_poc.repository.MessageRepository;
import com.ycyw.chat_poc.repository.SupportTicketRepository;
import jakarta.transaction.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static java.util.Arrays.asList;

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

    // Création ticket de support
    @PostMapping
    public ResponseEntity<SupportTicketDTO> create(@RequestBody NewSupportTicketPayload payload) {
        SupportTicket supportTicket = new SupportTicket();
        supportTicket.setUserPseudo(payload.getUserPseudo());
        supportTicket.setSubject(payload.getSubject());
        supportTicket.setStatus(ConversationStatus.WAITING);
        supportTicketRepository.save(supportTicket);

        Message message = new Message();
        message.setSupportTicket(supportTicket);
        message.setSenderPseudo(payload.getUserPseudo());
        message.setContent(payload.getMessage());
        messageRepository.save(message);

        // Envoi ticket dans le canal des tickets en attente pour les agents
        messagingTemplate.convertAndSend("/topic/supportTickets.waiting", supportTicket);

        // Envoi ticket dans le canal de la pastille pour mettre à jour historique côté utilisateur
        TicketSummaryDTO ticketSummaryDTO = new TicketSummaryDTO(
                supportTicket.getId(),
                supportTicket.getStatus(),
                supportTicket.getSubject(),
                Objects.equals(supportTicket.getAgentPseudo(), payload.getUserPseudo()) ? supportTicket.getUserPseudo() : supportTicket.getAgentPseudo(),
                supportTicket.getCreatedAt(),
                0
        );
        messagingTemplate.convertAndSend(
                "/topic/user." + payload.getUserPseudo() + ".badge",
                ticketSummaryDTO
        );

        SupportTicketDTO supportTicketDTO = new SupportTicketDTO(supportTicket.getId(), supportTicket.getSubject(), supportTicket.getCreatedAt());
        return ResponseEntity.ok(supportTicketDTO);
    }

    // Récupération listes des tickets en attente d'assignation
    @GetMapping("/waiting")
    public ResponseEntity<List<TicketSummaryDTO>> getWaitingSupportTickets() {
        List<SupportTicket> supportWaitingTickets = supportTicketRepository.findByStatusOrderByCreatedAtAsc(ConversationStatus.WAITING);
        List<TicketSummaryDTO> ticketSummaryDTOS = supportWaitingTickets.stream().map(
                (supportTicket ->
                        new TicketSummaryDTO(
                                supportTicket.getId(),
                                supportTicket.getStatus(),
                                supportTicket.getSubject(),
                                supportTicket.getUserPseudo(),
                                supportTicket.getCreatedAt(),
                                0
                        )
                )).toList();
        return ResponseEntity.ok(ticketSummaryDTOS);
    }

    // Assignation d'un ticket à un agent
    @PostMapping("/{id}/assign")
    public ResponseEntity<SupportTicketDTO> assign(@PathVariable Long id, @RequestParam String agentPseudo) {
        SupportTicket supportTicket = supportTicketRepository.findById(id).orElseThrow();

        if (supportTicket.getStatus() != ConversationStatus.WAITING) {
            throw new IllegalStateException("Conversation déjà assignée");
        }

        supportTicket.setAgentPseudo(agentPseudo);
        supportTicket.setStatus(ConversationStatus.ASSIGNED);
        supportTicket.setUpdatedAt(LocalDateTime.now());
        supportTicketRepository.save(supportTicket);

        // Envoi de l'id du ticket assigné dans le canal pour les supprimer côté front
        messagingTemplate.convertAndSend("/topic/supportTickets.assigned", supportTicket.getId());

        // Envoi de la mise à jour de statut à l'utilisateur
        TicketSummaryDTO ticketSummaryDTO = new TicketSummaryDTO(
                supportTicket.getId(),
                supportTicket.getStatus(),
                supportTicket.getSubject(),
                supportTicket.getUserPseudo(),
                supportTicket.getUpdatedAt(),
                0);

        messagingTemplate.convertAndSend("/topic/user." + supportTicket.getUserPseudo() + ".badge", ticketSummaryDTO);


        SupportTicketDTO supportTicketDTO = new SupportTicketDTO(supportTicket.getId(), supportTicket.getSubject(), supportTicket.getCreatedAt());
        return ResponseEntity.ok(supportTicketDTO);
    }

    // Récupération de l'historique de conversations agent et user
    @GetMapping
    public ResponseEntity<List<TicketSummaryDTO>> getHistory(@RequestParam String pseudo) {

        List<SupportTicket> supportTickets = supportTicketRepository.findByUserPseudoOrAgentPseudo(pseudo);

        List<TicketSummaryDTO> ticketSummaryDTOS = supportTickets.stream()
                .map(ticket -> new TicketSummaryDTO(
                        ticket.getId(),
                        ticket.getStatus(),
                        ticket.getSubject(),
                        Objects.equals(ticket.getAgentPseudo(), pseudo)
                                ? ticket.getUserPseudo()
                                : ticket.getAgentPseudo(),
                        ticket.getUpdatedAt(),
                        messageRepository.countUnread(ticket.getId(), pseudo)
                )).toList();

        return ResponseEntity.ok(ticketSummaryDTOS);
    }


    // Récupération d'une conversation avec les messages
    @GetMapping("/{id}/messages")
    public ResponseEntity<ConversationDTO> getMessages(@PathVariable Long id, @RequestParam String pseudo) {

        SupportTicket supportTicket = supportTicketRepository.findById(id).orElseThrow();

        boolean isParticipant = pseudo.equalsIgnoreCase(supportTicket.getUserPseudo())
                || pseudo.equalsIgnoreCase(supportTicket.getAgentPseudo());

        if (!isParticipant) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        List<Message> messages = messageRepository.findBySupportTicket_IdOrderBySentAtAsc(id);

        List<MessageDTO> messageDTOS = messages.stream().map(message ->
                new MessageDTO(
                        message.getId(),
                        message.getSenderPseudo(),
                        message.getContent(),
                        message.getSentAt(),
                        message.getReadAt())
        ).toList();

        ConversationDTO conversationDTO = new ConversationDTO(
                supportTicket.getId(),
                supportTicket.getSubject(),
                supportTicket.getUserPseudo(),
                supportTicket.getAgentPseudo(),
                messageDTOS);

        return ResponseEntity.ok(conversationDTO);
    }

    // Marquer tous les messages d'une conversation consultée comme lus
    @PostMapping("/{id}/read")
    @Transactional
    public void markAsRead(@PathVariable Long id,
                           @RequestParam String readerPseudo
    ) {
        SupportTicket supportTicket = supportTicketRepository.findById(id).orElseThrow();
        messageRepository.markAllAsRead(id, readerPseudo);
        TicketSummaryDTO ticketSummaryDTO = new TicketSummaryDTO(
                supportTicket.getId(),
                supportTicket.getStatus(),
                supportTicket.getSubject(),
                Objects.equals(supportTicket.getAgentPseudo(), readerPseudo) ? supportTicket.getUserPseudo() : supportTicket.getAgentPseudo(),
                supportTicket.getUpdatedAt(),
                0
        );
        messagingTemplate.convertAndSend(
                "/topic/user." + readerPseudo + ".badge",
                ticketSummaryDTO
        );
    }

    // Total de messages non lus pour la pastille global
    @GetMapping("/badge/total")
    public ResponseEntity<TotalUnreadDTO> getTotalUnread(@RequestParam String pseudo) {
        int totalUnread = messageRepository.countTotalUnread(pseudo);
        return ResponseEntity.ok(new TotalUnreadDTO(totalUnread));
    }
}
