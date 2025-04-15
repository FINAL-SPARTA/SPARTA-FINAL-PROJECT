package com.fix.game_service.infrastructure.client.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Builder
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ChatCreateRequest {

	private UUID gameId;
	private String gameName;
	private LocalDateTime gameDate;
	private String gameStatus;

}
