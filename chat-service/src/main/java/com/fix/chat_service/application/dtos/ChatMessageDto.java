package com.fix.chat_service.application.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

import com.fix.chat_service.domain.model.ChatMessage;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageDto {

    private String chatId;
    private Long userId;
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

	public void setNickname(String nickname) {
	    this.nickname = nickname;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public ChatMessage toChatMessage() {
        return ChatMessage.builder()
            .chatId(this.chatId)
            .userId(this.userId)
            .nickname(this.nickname)
            .message(this.message)
            .time(this.time)
            .type(this.type)
            .build();
    }
}
