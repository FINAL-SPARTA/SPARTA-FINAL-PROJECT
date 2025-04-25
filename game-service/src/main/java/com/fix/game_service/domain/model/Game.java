package com.fix.game_service.domain.model;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import com.fix.common_service.entity.Basic;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.criteria.CriteriaBuilder;
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
	private Team homeTeam;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private Team awayTeam;

	@Column(nullable = false)
	private LocalDateTime gameDate;

	@Column(nullable = false)
	private Long stadiumId;

	@Column(nullable = false)
	private String stadiumName;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private GameStatus gameStatus;

	@Column(nullable = false)
	private LocalDateTime openDate;

	@Column(nullable = false)
	private LocalDateTime closeDate;

	@OneToOne(mappedBy = "game", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	private GameRate gameRate;

	/**
	 * 경기 내용 수정
	 * @param updateGameInfo : 수정할 경기 내용
	 */
	public void updateGame(Game updateGameInfo) {
		Optional.ofNullable(updateGameInfo.getGameName()).ifPresent(gameName -> this.gameName = gameName);
		Optional.ofNullable(updateGameInfo.getHomeTeam()).ifPresent(gameTeam1 -> this.homeTeam = gameTeam1);
		Optional.ofNullable(updateGameInfo.getAwayTeam()).ifPresent(gameTeam2 -> this.awayTeam = gameTeam2);
		Optional.ofNullable(updateGameInfo.getGameDate()).ifPresent(gameDate -> this.gameDate = gameDate);
		Optional.ofNullable(updateGameInfo.getStadiumId()).ifPresent(stadiumId -> this.stadiumId = stadiumId);
		Optional.ofNullable(updateGameInfo.getStadiumName()).ifPresent(stadiumName -> this.stadiumName = stadiumName);
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

	/**
	 * 경기 기록 수정
	 * @param gameRate : 경기 기록
	 */
	public void updateGameRate(GameRate gameRate) {
		this.gameRate = gameRate;
	}
}