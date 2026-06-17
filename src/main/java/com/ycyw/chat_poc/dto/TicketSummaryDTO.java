package com.ycyw.chat_poc.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
@Getter
@Setter
public class TicketSummaryDTO {
    private Long id;
    private String otherPseudo;
    private LocalDateTime lastMessageAt;
    private int unreadCount;

    public TicketSummaryDTO(Long id, String otherPseudo, LocalDateTime lastMessageAt, int unreadCount) {
        this.id = id;
        this.otherPseudo = otherPseudo;
        this.lastMessageAt = lastMessageAt;
        this.unreadCount = unreadCount;

    }
}
