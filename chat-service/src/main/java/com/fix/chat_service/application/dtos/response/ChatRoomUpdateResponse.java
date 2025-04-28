package com.fix.chat_service.application.dtos.response;

import java.time.LocalDateTime;
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
public class ChatRoomUpdateResponse {

	private UUID chatId;
	private String gameName;
	private LocalDateTime chatOpenDate;

	public static ChatRoomUpdateResponse fromChatRoom(ChatRoom chatRoom) {
		return ChatRoomUpdateResponse.builder()
			.chatId(chatRoom.getChatRoomId())
			.gameName(chatRoom.getGameName())
			.chatOpenDate(chatRoom.getChatOpenDate())
			.build();
	}
}
