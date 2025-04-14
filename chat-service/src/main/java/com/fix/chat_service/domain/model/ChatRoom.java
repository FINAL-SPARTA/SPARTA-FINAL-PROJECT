package com.fix.chat_service.domain.model;

import java.time.LocalDateTime;
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
public class ChatRoom {

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

}

