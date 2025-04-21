package com.fix.game_service.application.service;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fix.common_service.kafka.dto.TicketUpdatedPayload;
import com.fix.game_service.application.exception.GameException;
import com.fix.game_service.domain.model.GameRate;
import com.fix.game_service.domain.repository.GameRateRepository;
import com.fix.game_service.domain.repository.GameRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ConsumerService {

	private final GameRateRepository gameRateRepository;

	@Transactional
	public void updateGameSeatsByConsumer(TicketUpdatedPayload payload) {
		// 해당 경기 탐색
		GameRate game = findGameRate(payload.getGameId());

		// 전체 좌석
		Integer totalSeats = game.getTotalSeats();
		// 좌석을 변경할 수량
		int quantity = payload.getQuantity();

		// 잔여 좌석
		Integer newRemainingSeats;

		// 경기의 잔여 좌석에 따라서 잔여좌석 표시 상태 변경
		if (game.getRemainingSeats() == null) {
			newRemainingSeats = game.getTotalSeats() + quantity;
		} else {
			newRemainingSeats = game.getRemainingSeats() + quantity;
		}

		// 예매율 계산
		Double newAdvanceReservation = (double) (newRemainingSeats / totalSeats);

		game.updateGameSeats(newRemainingSeats, newAdvanceReservation);
	}

	/**
	 * 경기 찾기
	 * @param gameId : 찾을 경기 ID
	 * @return : 찾은 경기 내용 반환
	 */
	private GameRate findGameRate(UUID gameId) {
		GameRate gameRate = gameRateRepository.findById(gameId).orElseThrow(() -> new GameException(
			GameException.GameErrorType.GAME_NOT_FOUND));

		return gameRate;
	}
}
