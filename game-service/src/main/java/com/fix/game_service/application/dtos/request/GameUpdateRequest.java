package com.fix.game_service.application.dtos.request;

import java.time.LocalDateTime;
import java.util.UUID;

import com.fix.game_service.domain.model.Game;
import com.fix.game_service.domain.model.GameStatus;
import com.fix.game_service.domain.model.Team;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GameUpdateRequest {

	private String gameName;
	private Team homeTeam;
	private Team awayTeam;

	private LocalDateTime gameDate;
	private UUID stadiumId;

	private GameStatus gameStatus;

	private LocalDateTime openDate;
	private LocalDateTime closeDate;

	public Game toGame() {
		return Game.builder()
			.gameName(this.gameName)
			.homeTeam(this.homeTeam)
			.awayTeam(this.awayTeam)
			.gameDate(this.gameDate)
			.stadiumId(this.stadiumId)
			.gameStatus(this.gameStatus)
			.openDate(this.openDate)
			.closeDate(this.closeDate)
			.build();
	}

}
