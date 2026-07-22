package com.ycyw.chat_poc.dto;

import com.ycyw.chat_poc.models.ConversationStatus;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class TicketSummaryDTO {
    private Long id;
    private ConversationStatus status;
    private String subject;
    private String otherPseudo;
    private LocalDateTime lastMessageAt;
    private int unreadCount;

    public TicketSummaryDTO(Long id, ConversationStatus status, String subject, String otherPseudo, LocalDateTime lastMessageAt, int unreadCount) {
        this.id = id;
        this.status = status;
        this.subject = subject;
        this.otherPseudo = otherPseudo;
        this.lastMessageAt = lastMessageAt;
        this.unreadCount = unreadCount;

    }
}
