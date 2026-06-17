package com.ycyw.chat_poc.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NewMessagePayload {
    private Long conversationId;
    private Boolean fromAgent;
    private String content;
}
