package com.ycyw.chat_poc.dto;

import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class MessageDTO {
    final private Long id;
    final private String senderPseudo;
    final private String content;
    final private LocalDateTime sentAt;
    final private LocalDateTime readAt;

    public MessageDTO(Long id, String senderPseudo, String content, LocalDateTime sentAt, LocalDateTime readAt) {
        this.id = id;
        this.senderPseudo = senderPseudo;
        this.content = content;
        this.sentAt = sentAt;
        this.readAt = readAt;
    }
}
