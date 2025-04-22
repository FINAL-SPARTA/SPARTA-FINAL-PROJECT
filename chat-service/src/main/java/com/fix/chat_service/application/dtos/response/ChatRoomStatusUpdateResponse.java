package com.fix.chat_service.application.dtos.response;

import java.util.UUID;

import com.fix.chat_service.domain.model.ChatRoom;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatRoomStatusUpdateResponse {

	private UUID chatId;
	private String gameStatus;

	public static ChatRoomStatusUpdateResponse fromChatRoom(ChatRoom chatRoom) {
		return ChatRoomStatusUpdateResponse.builder()
			.chatId(chatRoom.getChatRoomId())
			.gameStatus(chatRoom.getGameStatus())
			.build();
	}
}
