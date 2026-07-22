package com.ycyw.chat_poc.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TotalUnreadDTO {
    private int totalUnread;

    public TotalUnreadDTO(int totalUnread) {
        this.totalUnread = totalUnread;
    }
}
