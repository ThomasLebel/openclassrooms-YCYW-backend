package com.ycyw.chat_poc.dto;

import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class MessageDTO {
    final private Long id;
    final private boolean fromAgent;
    final private String content;
    final private LocalDateTime sentAt;
    final private LocalDateTime readAt;

    public MessageDTO(Long id,  boolean fromAgent, String content, LocalDateTime sentAt, LocalDateTime readAt) {
        this.id = id;
        this.fromAgent = fromAgent;
        this.content = content;
        this.sentAt = sentAt;
        this.readAt = readAt;
    }
}
