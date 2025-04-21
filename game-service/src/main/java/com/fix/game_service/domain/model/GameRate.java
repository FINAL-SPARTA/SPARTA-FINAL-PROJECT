package com.fix.game_service.domain.model;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@Entity
@Table(name = "p_game_rate")
@NoArgsConstructor
@AllArgsConstructor

public class GameRate {

	@Id
	private UUID gameRateId;

	@Column
	private Double advanceReservation;  // 예매율

	@Column
	private Integer remainingSeats;     // 남은 좌석

	@Column(nullable = false)
	private Integer totalSeats;         // 총 좌석

	/**
	 * 경기 잔여 좌석 및 예매율 수정
	 * @param newRemainingSeats : 잔여 좌석
	 * @param newAdvanceReservation : 예매율
	 */
	public void updateGameSeats(Integer newRemainingSeats, Double newAdvanceReservation) {
		this.remainingSeats = newRemainingSeats;
		this.advanceReservation = newAdvanceReservation;
	}

	/**
	 * 경기장 수정에 따른 총 좌석 수 수정
	 * @param seatQuantity : 총 좌석 수
	 */
	public void updateStatus(Integer seatQuantity) {
		this.totalSeats = seatQuantity;
	}
}
