package com.fix.game_service.presentation.controller;

import org.springframework.kafka.annotation.KafkaListener;

import com.fix.game_service.application.dtos.request.GameTicketRequest;
import com.fix.game_service.application.service.GameService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class ConsumerController {

	private final GameService gameService;

	// Kafka에서 메시지를 소비하는 리스너 메서드들을 정의
	// @KafkaListener 어노테이션은 각 메서드를 Kafka 리스너로 설정
	@KafkaListener(topics = "game-events", groupId = "group_a")
	public void consume(Object data) {
		if (data instanceof GameTicketRequest dto) {
			log.info("Received DTO: {}", dto);
			gameService.updateGameSeats(dto.getGameId(), dto.getQuantity());
		} else {
			log.warn("Unknown message type: {}", data);
		}
	}

}

