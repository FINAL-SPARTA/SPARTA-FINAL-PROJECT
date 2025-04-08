package com.fix.game_service.domain;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import com.fix.common_service.entity.Basic;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@Entity
@Table(name = "p_game")
@NoArgsConstructor
@AllArgsConstructor
public class Game extends Basic {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID gameId;

	@Column(nullable = false)
	private String gameName;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private Team gameTeam1;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private Team gameTeam2;

	@Column(nullable = false)
	private LocalDateTime gameDate;

	@Column(nullable = false)
	private UUID stadiumId;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private GameStatus gameStatus;

	@Column(nullable = false)
	private LocalDateTime openDate;

	@Column(nullable = false)
	private LocalDateTime closeDate;

	@Column
	private Double advanceReservation;  // 예매율

	@Column
	private Long remainingSeats;        // 남은 좌석

	/**
	 * 경기 내용 수정
	 * @param updateGameInfo : 수정할 경기 내용
	 */
	public void updateGame(Game updateGameInfo) {
		Optional.ofNullable(updateGameInfo.getGameName()).ifPresent(gameName -> this.gameName = gameName);
		Optional.ofNullable(updateGameInfo.getGameTeam1()).ifPresent(gameTeam1 -> this.gameTeam1 = gameTeam1);
		Optional.ofNullable(updateGameInfo.getGameTeam2()).ifPresent(gameTeam2 -> this.gameTeam2 = gameTeam2);
		Optional.ofNullable(updateGameInfo.getGameDate()).ifPresent(gameDate -> this.gameDate = gameDate);
		Optional.ofNullable(updateGameInfo.getStadiumId()).ifPresent(stadiumId -> this.stadiumId = stadiumId);
		Optional.ofNullable(updateGameInfo.getGameStatus()).ifPresent(gameStatus -> this.gameStatus = gameStatus);
		Optional.ofNullable(updateGameInfo.getOpenDate()).ifPresent(openDate -> this.openDate = openDate);
		Optional.ofNullable(updateGameInfo.getCloseDate()).ifPresent(closeDate -> this.closeDate = closeDate);
	}

	/**
	 * 경기 상태 수정
	 * @param updateGameStatusInfo : 수정할 경기 상태 내용
	 */
	public void updateGameStatus(Game updateGameStatusInfo) {
		Optional.ofNullable(updateGameStatusInfo.getGameStatus()).ifPresent(gameStatus -> this.gameStatus = gameStatus);
	}
}