package com.fix.game_service.presentation.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fix.common_service.kafka.dto.EventKafkaMessage;
import com.fix.common_service.kafka.dto.TicketUpdatedPayload;
import com.fix.common_service.kafka.producer.KafkaProducerHelper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class TicketGameProducer {

	private final KafkaProducerHelper kafkaProducerHelper;

	@Value("${kafka-topics.ticket.updated-dlq}")
	private String ticketUpdateFailTopic;

	public void sendTicketUpdatedFailEvent(TicketUpdatedPayload payload, Integer retryCount) {
		EventKafkaMessage<TicketUpdatedPayload> eventKafkaMessage = new EventKafkaMessage<>("TICKET_UPDATED_FAILED|" + retryCount, payload);

		kafkaProducerHelper.send(ticketUpdateFailTopic, payload.getGameId().toString(), eventKafkaMessage);
	}

}
