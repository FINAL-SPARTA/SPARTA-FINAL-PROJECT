package com.fix.chat_service.application.dtos.request;

import java.time.LocalDateTime;

import com.fix.chat_service.domain.model.ChatRoom;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatRoomUpdateRequest {

	private String gameName;
	private LocalDateTime gameDate;

	public ChatRoom toChatRoom() {
		return ChatRoom.builder()
			.gameName(gameName)
			.gameDate(gameDate)
			.chatOpenDate(gameDate.minusMinutes(10))
			.chatCloseDate(gameDate.plusDays(1))
			.build();
	}

}
