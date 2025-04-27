package com.fix.game_service.presentation.controller;

import com.fix.common_service.kafka.dto.EventKafkaMessage;
import com.fix.common_service.kafka.dto.GameChatPayload;
import com.fix.common_service.kafka.dto.GameCreatedInfoPayload;
import com.fix.common_service.kafka.producer.KafkaProducerHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class GameProducer {

    private final KafkaProducerHelper kafkaProducerHelper;

    @Value("${kafka-topics.game.created}")
    private String gameAlarmTopic;

    @Value("${kafka-topics.game.chat}")
    private String gameChatTopic;

    public void sendGameInfoToAlarm(GameCreatedInfoPayload payload) {
        EventKafkaMessage<GameCreatedInfoPayload> eventKafkaMessage = new EventKafkaMessage<>("GAME_CREATED", payload);

        kafkaProducerHelper.send(gameAlarmTopic, payload.getGameId().toString(), eventKafkaMessage);
    }

    public void sendGameInfoToChat(GameChatPayload payload) {
        EventKafkaMessage<GameChatPayload> eventKafkaMessage = new EventKafkaMessage<>("GAME_CHAT_CREATED", payload);

        kafkaProducerHelper.send(gameChatTopic, payload.getGameId().toString(), eventKafkaMessage);
    }

}
