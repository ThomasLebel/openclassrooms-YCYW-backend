package com.ycyw.chat_poc.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
@Getter
@Setter
public class ConversationDTO {
    final private Long id;
    final private String subject;
    final private String userPseudo;
    final private String agentPseudo;
    final private List<MessageDTO> messages;

    public ConversationDTO(Long id, String subject, String userPseudo, String agentPseudo, List<MessageDTO> messages) {
        this.id = id;
        this.subject = subject;
        this.userPseudo = userPseudo;
        this.agentPseudo = agentPseudo;
        this.messages = messages;
    }

}
