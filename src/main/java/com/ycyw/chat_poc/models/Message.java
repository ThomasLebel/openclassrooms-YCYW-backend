package com.ycyw.chat_poc.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "messages")
public class Message {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "ticket_id")
    private SupportTicket supportTicket;

    @Column(name = "is_from_agent", nullable = false)
    private Boolean isFromAgent;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "sent_at", nullable = false)
    private LocalDateTime sentAt =  LocalDateTime.now();

    @Column(name="read_at")
    private LocalDateTime readAt;
}
