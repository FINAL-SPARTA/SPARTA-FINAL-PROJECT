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
public class ChatRoomGetOneResponse {

	private UUID chatId;
	private String gameName;
	private String message;

	public static ChatRoomGetOneResponse fromChatRoom(ChatRoom chatRoom) {
		return ChatRoomGetOneResponse
			.builder()
			.chatId(chatRoom.getChatRoomId())
			.gameName(chatRoom.getGameName())
			.message("채팅방 입장 성공")
			.build();
	}
}
