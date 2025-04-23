package com.fix.game_service.presentation.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fix.common_service.kafka.dto.EventKafkaMessage;
import com.fix.common_service.kafka.dto.GameCreatedInfoPayload;
import com.fix.common_service.kafka.producer.KafkaProducerHelper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class GameProducer {

	private final KafkaProducerHelper kafkaProducerHelper;

	@Value("${kafka-topics.game.created}")
	private String gameAlarmTopic;

	public void sendGameInfoToAlarm(GameCreatedInfoPayload payload) {
		EventKafkaMessage<GameCreatedInfoPayload> eventKafkaMessage = new EventKafkaMessage<>("GAME_CREATED", payload);

		kafkaProducerHelper.send(gameAlarmTopic, payload.getGameId().toString(), eventKafkaMessage);
	}

}
