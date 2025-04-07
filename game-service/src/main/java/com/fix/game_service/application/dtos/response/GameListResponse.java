package com.fix.game_service.application.dtos.response;

import java.time.LocalDateTime;
import java.util.UUID;

import com.fix.game_service.domain.Game;
import com.fix.game_service.domain.Team;

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
	private Team gameTeam1;
	private Team gameTeam2;
	private LocalDateTime gameDate;
	private UUID stadiumId;

	public static GameListResponse fromGame(Game game) {
		return GameListResponse.builder()
			.gameId(game.getGameId())
			.gameTeam1(game.getGameTeam1())
			.gameTeam2(game.getGameTeam2())
			.gameDate(game.getGameDate())
			.stadiumId(game.getStadiumId())
			.build();
	}
}

