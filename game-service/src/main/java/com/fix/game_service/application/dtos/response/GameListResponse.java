package com.fix.game_service.application.dtos.response;

import java.time.LocalDateTime;
import java.util.UUID;

import com.fix.game_service.domain.model.Game;
import com.fix.game_service.domain.model.Team;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GameListResponse {

	private UUID gameId;
	private Team homeTeam;
	private Team awayTeam;
	private LocalDateTime gameDate;
	private Long stadiumId;

	public static GameListResponse fromGame(Game game) {
		return GameListResponse.builder()
			.gameId(game.getGameId())
			.homeTeam(game.getHomeTeam())
			.awayTeam(game.getAwayTeam())
			.gameDate(game.getGameDate())
			.stadiumId(game.getStadiumId())
			.build();
	}
}

