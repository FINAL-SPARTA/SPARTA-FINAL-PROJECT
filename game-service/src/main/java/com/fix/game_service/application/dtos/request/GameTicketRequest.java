package com.fix.game_service.application.dtos.request;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GameTicketRequest {

	private UUID gameId;
	private int quantity;

}
