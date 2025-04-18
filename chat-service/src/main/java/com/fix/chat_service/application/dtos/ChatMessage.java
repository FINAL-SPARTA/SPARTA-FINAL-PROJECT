package com.fix.chat_service.application.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessage {

    private String chatId;
    private String nickname;
    private String message;
    private LocalDateTime time;
    private String type;

    public void setMessageType(String type) {
        this.type = type;
    }

    public void setChatId(String chatId) {
        this.chatId = chatId;
    }

}
