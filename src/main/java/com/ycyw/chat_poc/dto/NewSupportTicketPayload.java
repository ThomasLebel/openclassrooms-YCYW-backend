package com.ycyw.chat_poc.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NewSupportTicketPayload {
    private String userPseudo;
    private String subject;
    private String message;
}
