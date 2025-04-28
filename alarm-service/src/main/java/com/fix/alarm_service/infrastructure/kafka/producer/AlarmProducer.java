package com.fix.alarm_service.infrastructure.kafka.producer;


import com.fix.common_service.kafka.dto.AlarmGameStartedPayload;
import com.fix.common_service.kafka.dto.EventKafkaMessage;
import com.fix.common_service.kafka.producer.KafkaProducerHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class AlarmProducer {

    private final KafkaProducerHelper kafkaProducerHelper;
    @Value("${kafka-topics.alarm.game-started}")
    private String alarmGameStartedTopic;


    public void sendGameIdToOrder(UUID gameId){

        AlarmGameStartedPayload payload = new AlarmGameStartedPayload(gameId);
        EventKafkaMessage <AlarmGameStartedPayload> event = new EventKafkaMessage<>("ALARM_GAME_STARTED",payload);

        kafkaProducerHelper.send(alarmGameStartedTopic,gameId.toString(), event);
        log.info("[Producer] OrderService 로 경기 알림 이벤트 발행 완료 - gameId: {}",gameId);


    }

}
