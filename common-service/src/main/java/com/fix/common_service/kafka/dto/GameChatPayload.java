package com.fix.common_service.kafka.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GameChatPayload {

	private UUID gameId;
	private String gameName;
	private LocalDateTime gameDate;
	private String gameStatus;

}
