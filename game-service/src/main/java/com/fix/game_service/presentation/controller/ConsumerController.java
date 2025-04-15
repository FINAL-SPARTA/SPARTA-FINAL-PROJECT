package com.fix.game_service.presentation.controller;

import com.fix.common_service.dto.EventKafkaMessage;
import com.fix.game_service.application.service.GameService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;

@Slf4j
@RequiredArgsConstructor
public class ConsumerController {

	private final GameService gameService;

	/**
	 * Kafka에서 메시지 소비를 위한 리스너 메서드 정의
	 * @param data : 주고 받을 대화
	 */
	@KafkaListener(topics = "ticket-updated-topic", groupId = "game-service-group")
	// 각 메서드들을 Kafka 리스너로 설정
	public void consume(Object data) {
		if (data instanceof EventKafkaMessage message) {
			log.info("Received DTO: {}", message);
			gameService.updateGameSeatsByConsumer(message);
		} else {
			log.warn("Unknown message type: {}", data);
		}
	}

}

