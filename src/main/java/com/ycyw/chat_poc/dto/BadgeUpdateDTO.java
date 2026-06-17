package com.ycyw.chat_poc.dto;

public class BadgeUpdateDTO {
    private Long ticketSupportId;
    private int unreadCount;

    public BadgeUpdateDTO(Long ticketSupportId, int unreadCount) {
        this.ticketSupportId = ticketSupportId;
        this.unreadCount = unreadCount;
    }
}
