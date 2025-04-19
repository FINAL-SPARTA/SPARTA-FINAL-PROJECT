package com.fix.game_service.presentation.controller;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;

import com.fix.common_service.kafka.consumer.AbstractKafkaConsumer;
import com.fix.common_service.kafka.consumer.RedisIdempotencyChecker;
import com.fix.common_service.kafka.dto.EventKafkaMessage;
import com.fix.common_service.kafka.dto.TicketUpdatedPayload;
import com.fix.game_service.application.service.ConsumerService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TicketGameConsumer extends AbstractKafkaConsumer<TicketUpdatedPayload> {

	private final ConsumerService consumerService;
	private final TicketGameProducer ticketGameProducer;

	public TicketGameConsumer(RedisIdempotencyChecker idempotencyChecker,
		ConsumerService consumerService, TicketGameProducer ticketGameProducer) {
		super(idempotencyChecker);
		this.consumerService = consumerService;
		this.ticketGameProducer = ticketGameProducer;
	}

	/**
	 * Kafka에서 메시지 소비를 위한 리스너 메서드 정의
	 * @param message : 이벤트에서 수신할 데이터
	 */
	@KafkaListener(topics = "${kafka-topic.ticket.updated}", groupId = "game-service-group")
	public void updateGameSeatsByConsumer(ConsumerRecord<String, EventKafkaMessage<TicketUpdatedPayload>> record,
		EventKafkaMessage<TicketUpdatedPayload> message,
		Acknowledgment acknowledgment) {
		log.info("[Kafka] 티켓 이벤트 수신 : {}", message.getEventType());
		super.consume(record, message, acknowledgment);
	}

	@Override
	protected void processPayload(Object rawPayload) {
		TicketUpdatedPayload payload = mapPayload(rawPayload, TicketUpdatedPayload.class);
		try {
			consumerService.updateGameSeatsByConsumer(payload);
		} catch (Exception e) {
			// 실패 시 DLQ 처리
			ticketGameProducer.sendTicketUpdatedFailEvent(payload, 0);
		}
	}

	/**
	 * Ticket 이벤트 처리 실패 시 재처리할 DLQ
	 * @param record : 재처리할 로직
	 */
	@KafkaListener(topics = "${kafka-topic.ticket.updated.dlq}", groupId = "game-service-group")
	public void updateGameSeatsRetry(ConsumerRecord<String, EventKafkaMessage<TicketUpdatedPayload>> record,
		EventKafkaMessage<TicketUpdatedPayload> message) {
		// 1. 재처리 최대 3회까지 적용
		Integer retryCount = getRetryCount(message.getEventType());
		if (retryCount >= 3) {
			log.error("DLQ 재처리 3회 실패 : {} ", record.value());
			// TODO : 로그 저장해두기
			return;
		}
		
		// 2. 재처리 시도
		TicketUpdatedPayload payload = null;
		try {
			payload = mapPayload(record, TicketUpdatedPayload.class);
			consumerService.updateGameSeatsByConsumer(payload);
		} catch (Exception e) {
			ticketGameProducer.sendTicketUpdatedFailEvent(payload, retryCount + 1);
		}
	}

	/**
	 * 재시도 횟수 구하기
	 * @param eventType : Kafka 이벤트 타입 (횟수 포함)
	 * @return : 재시도 횟수 반환
	 */
	private Integer getRetryCount(String eventType) {
		String num = eventType.split("\\|")[1];

		if (num != null) {
			return Integer.parseInt(num);
		}

		return 0;
	}
}

