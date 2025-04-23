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
public class GameCreatedInfoPayload {

	private UUID gameId;
	private LocalDateTime gameDate;
	private String gameStatus;

}
