package com.fix.game_service.application.service;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fix.common_service.kafka.dto.EventKafkaMessage;
import com.fix.common_service.kafka.dto.TicketUpdatedPayload;
import com.fix.game_service.application.exception.GameException;
import com.fix.game_service.domain.model.Game;
import com.fix.game_service.domain.repository.GameRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ConsumerService {

	private final GameRepository gameRepository;

	@Transactional
	public void updateGameSeatsByConsumer(EventKafkaMessage message) {
		log.info("[Kafka] 티켓 이벤트 수신 : {}", message.getEventType());

		// 수정할 경기 내용 가져오기
		TicketUpdatedPayload payload = (TicketUpdatedPayload) message.getPayload();
		// 해당 경기 탐색
		Game game = findGame(payload.getGameId());

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
	private Game findGame(UUID gameId) {
		Game game = gameRepository.findByIdWithLock(gameId);
		if (game != null) {
			return game;
		} else {
			throw new GameException(GameException.GameErrorType.GAME_NOT_FOUND);
		}
	}
}
