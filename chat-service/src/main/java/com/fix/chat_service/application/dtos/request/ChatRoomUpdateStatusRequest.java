package com.fix.chat_service.application.dtos.request;

import com.fix.chat_service.domain.model.ChatRoom;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatRoomUpdateStatusRequest {

	private String gameStatus;

	public ChatRoom toChatRoom() {
		return ChatRoom.builder()
			.gameStatus(gameStatus)
			.build();
	}

}
