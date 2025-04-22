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
public class ChatRoomListResponse {

	private UUID chatId;
	private String gameName;
	private LocalDateTime chatOpenDate;

	public ChatRoomListResponse(ChatRoom chatRoom) {
		this.chatId = chatRoom.getChatRoomId();
		this.gameName = chatRoom.getGameName();
		this.chatOpenDate = chatRoom.getChatOpenDate();
	}

}
