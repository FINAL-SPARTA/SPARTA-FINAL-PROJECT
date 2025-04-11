package com.fix.game_service.application.dtos.request;

import com.fix.game_service.domain.model.Game;
import com.fix.game_service.domain.model.GameStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GameStatusUpdateRequest {

	private GameStatus gameStatus;

	public Game toGame() {
		return Game.builder()
			.gameStatus(this.gameStatus)
			.build();
	}
}
