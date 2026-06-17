package com.ycyw.chat_poc.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class SupportTicketDTO {
    private long id;
    private String subject;
    private LocalDateTime createdAt;

    public SupportTicketDTO(long id, String subject, LocalDateTime createdAt) {
        this.id = id;
        this.subject = subject;
        this.createdAt = createdAt;
    }
}
