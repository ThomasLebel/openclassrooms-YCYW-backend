package com.ycyw.chat_poc.controller;

import com.ycyw.chat_poc.dto.BadgeUpdateDTO;
import com.ycyw.chat_poc.dto.NewMessagePayload;
import com.ycyw.chat_poc.models.Message;
import com.ycyw.chat_poc.models.SupportTicket;
import com.ycyw.chat_poc.repository.MessageRepository;
import com.ycyw.chat_poc.repository.SupportTicketRepository;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.time.LocalDateTime;

@Controller
public class MessageController {
    private final SimpMessagingTemplate messagingTemplate;
    private final MessageRepository messageRepository;
    private final SupportTicketRepository supportTicketRepository;

    public MessageController(SimpMessagingTemplate messagingTemplate, MessageRepository messageRepository, SupportTicketRepository supportTicketRepository){
        this.messagingTemplate = messagingTemplate;
        this.messageRepository = messageRepository;
        this.supportTicketRepository = supportTicketRepository;
    }

    @MessageMapping("/message.send")
    public void sendMessage(NewMessagePayload payload){
        SupportTicket supportTicket = supportTicketRepository.findById(payload.getConversationId()).orElseThrow();

        // Persister le message
        Message message = new Message();
        message.setSupportTicket(supportTicket);
        message.setContent(payload.getContent());
        message.setIsFromAgent(payload.getFromAgent());
        messageRepository.save(message);

        // Mettre à jour date dernier message ticket
        supportTicket.setUpdatedAt(LocalDateTime.now());
        supportTicketRepository.save(supportTicket);

        // Diffuser sur le canal du chat en temps réel
        messagingTemplate.convertAndSend("topic/supportTicket." + supportTicket.getId(), message);

        // Diffuser sur le canal de la pastille nombre de messages non lu
        String pseudo = payload.getFromAgent() ? supportTicket.getUserPseudo() : supportTicket.getAgentPseudo();
        if (pseudo != null) {
            int unreadCount = messageRepository.countUnread(supportTicket.getId(), payload.getFromAgent());
            messagingTemplate.convertAndSend("topic/user." + pseudo + ".badge", new BadgeUpdateDTO(supportTicket.getId(), unreadCount));
        }

    }
}
