package com.fix.chat_service.domain.model;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@Entity
@Table(name = "p_chatroom")
@NoArgsConstructor
@AllArgsConstructor
public class ChatRoom extends Basic {

	@Id
	@Column(name = "chat_room_id")
	private UUID chatRoomId;

	@Column(name = "game_id")
	private UUID gameId;

	@Column(name = "game_name")
	private String gameName;

	@Column(name = "game_date")
	private LocalDateTime gameDate;

	@Column(name = "chat_open_date")
	private LocalDateTime chatOpenDate;

	@Column(name = "chat_close_date")
	private LocalDateTime chatCloseDate;

	@Column(name = "game_status")
	private String gameStatus;

	/**
	 * 채팅방 내용 수정
	 * @param updateChatRoom : 수정할 채팅방 내용
	 */
	public void updateChatRoomInfo(ChatRoom updateChatRoom) {
		Optional.ofNullable(updateChatRoom.getGameName()).ifPresent(gameName -> this.gameName = gameName);
		Optional.ofNullable(updateChatRoom.getGameDate()).ifPresent(gameDate -> this.gameDate = gameDate);
		Optional.ofNullable(updateChatRoom.getChatOpenDate())
			.ifPresent(chatOpenDate -> this.chatOpenDate = chatOpenDate);
		Optional.ofNullable(updateChatRoom.getChatCloseDate())
			.ifPresent(chatCloseDate -> this.chatCloseDate = chatCloseDate);
	}

	/**
	 * 채팅방 상태(경기 상태) 수정
	 * @param updateChatRoomStatus : 수정할 채팅방(경기) 상태
	 */
	public void updateChatRoomStatus(ChatRoom updateChatRoomStatus) {
		Optional.ofNullable(updateChatRoomStatus.getGameStatus()).ifPresent(gameStatus -> this.gameStatus = gameStatus);
	}

}

