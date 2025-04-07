package com.fix.game_service.domain;

import java.time.LocalDateTime;
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

}