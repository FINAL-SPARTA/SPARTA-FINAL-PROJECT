package com.fix.game_service.application.dtos.response;

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
public class GameCreateResponse {

	private UUID gameId;
	private String gameName;
	private Team homeTeam;
	private Team awayTeam;
	private LocalDateTime gameDate;
	private UUID stadiumId;
	private GameStatus gameStatus;
	private LocalDateTime openDate;
	private LocalDateTime closeDate;

	public static GameCreateResponse fromGame(Game game) {
		return GameCreateResponse.builder()
			.gameId(game.getGameId())
			.gameName(game.getGameName())
			.homeTeam(game.getHomeTeam())
			.awayTeam(game.getAwayTeam())
			.gameDate(game.getGameDate())
			.stadiumId(game.getStadiumId())
			.gameStatus(game.getGameStatus())
			.openDate(game.getOpenDate())
			.closeDate(game.getCloseDate())
			.build();
	}

}

