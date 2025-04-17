package com.fix.game_service.presentation.controller;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.header.Headers;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;

import com.fix.common_service.dto.EventKafkaMessage;
import com.fix.game_service.application.service.ConsumerService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class ConsumerController {

	private final ConsumerService consumerService;
	private final KafkaTemplate<String, Object> kafkaTemplate;

	/**
	 * Kafka에서 메시지 소비를 위한 리스너 메서드 정의
	 * @param message : 이벤트에서 수신할 데이터
	 */
	@KafkaListener(topics = "ticket-updated-topic", groupId = "game-service-group")
	public void updateGameSeatsByConsumer(EventKafkaMessage message) {
		try {
			consumerService.updateGameSeatsByConsumer(message);
		} catch (Exception e) {
			// 실패 시 DLQ 처리
			ProducerRecord<String, Object> dlqRecord = new ProducerRecord<>("game-ticket.dlq", message);
			kafkaTemplate.send(dlqRecord);
		}
	}

	/**
	 * Ticket 이벤트 처리 실패 시 재처리할 DLQ
	 * @param record : 재처리할 로직
	 */
	@KafkaListener(topics = "game-ticket.dlq", groupId = "game-service-group")
	public void updateGameSeatsRetry(ConsumerRecord<String, EventKafkaMessage> record) {
		// 1. 재처리 최대 3회까지 적용
		Integer retryCount = getRetryCount(record.headers());
		if (retryCount >= 3) {
			log.error("DLQ 재처리 3회 실패 : {} ", record.value());
			// TODO : 로그 저장해두기
			return;
		}
		
		// 2. 재처리 시도
		try {
			consumerService.updateGameSeatsByConsumer(record.value());
		} catch (Exception e) {
			// 실패했다면 다시 DLQ 전송
			ProducerRecord<String, Object> dlqRecord = new ProducerRecord<>("game-ticket.dlq", record.value());
			dlqRecord.headers().add("retry-count", (retryCount + 1 + "").getBytes());
			kafkaTemplate.send(dlqRecord);
		}
	}

	/**
	 * 재시도 횟수 구하기
	 * @param headers : Kakfa Header
	 * @return : 재시도 횟수 반환
	 */
	private Integer getRetryCount(Headers headers) {
		Header header = headers.lastHeader("retry-count");
		if (header != null) {
			return Integer.parseInt(new String(header.value()));
		}
		return 0;
	}
}

