package com.fix.game_service.application.dtos.request;

import java.time.LocalDateTime;
import java.util.UUID;

import com.fix.game_service.domain.Game;
import com.fix.game_service.domain.GameStatus;
import com.fix.game_service.domain.Team;

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
	private Team gameTeam1;
	private Team gameTeam2;

	private LocalDateTime gameDate;
	private UUID stadiumId;

	private GameStatus gameStatus;

	private LocalDateTime openDate;
	private LocalDateTime closeDate;

	public Game toGame() {
		return Game.builder()
			.gameName(this.gameName)
			.gameTeam1(this.gameTeam1)
			.gameTeam2(this.gameTeam2)
			.gameDate(this.gameDate)
			.stadiumId(this.stadiumId)
			.gameStatus(this.gameStatus)
			.openDate(this.openDate)
			.closeDate(this.closeDate)
			.build();
	}

}
