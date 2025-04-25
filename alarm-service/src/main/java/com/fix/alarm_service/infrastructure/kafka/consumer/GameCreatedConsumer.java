package com.fix.alarm_service.infrastructure.kafka.consumer;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.fix.alarm_service.application.service.AlarmService;
import com.fix.alarm_service.domain.model.GameAlarmSchedule;
import com.fix.common_service.kafka.consumer.AbstractKafkaConsumer;
import com.fix.common_service.kafka.consumer.RedisIdempotencyChecker;
import com.fix.common_service.kafka.dto.EventKafkaMessage;
import com.fix.common_service.kafka.dto.GameCreatedInfoPayload;
import jakarta.inject.Qualifier;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class GameCreatedConsumer extends AbstractKafkaConsumer<GameCreatedInfoPayload> {

    private final AlarmService alarmService;

    private final ObjectMapper objectMapper;

    public GameCreatedConsumer(ObjectMapper objectMapper, RedisIdempotencyChecker idempotencyChecker, AlarmService alarmService) {
        super(idempotencyChecker);
        this.objectMapper = objectMapper;
        this.alarmService = alarmService;
    }

    @KafkaListener(
            topics = "${kafka-topics.game.created}",
            groupId = "alarm-service-game-created-consumer"
    )
    public void listen(ConsumerRecord<String, EventKafkaMessage<GameCreatedInfoPayload>> record,
                       EventKafkaMessage<GameCreatedInfoPayload> message,
                       Acknowledgment ack) {
        super.consume(record, message, ack);
    }


    @Override
    protected void processPayload(Object rawPayload) {
        try {
            GameCreatedInfoPayload payload = objectMapper.convertValue(rawPayload, GameCreatedInfoPayload.class);
            GameAlarmSchedule schedule = GameAlarmSchedule.of(payload.getGameId(), payload.getGameDate());
            alarmService.saveSchedule(schedule);
            log.info("[Kafka] 경기 알림 스케줄 저장 완료 - gameId: {}, gameDate: {}", payload.getGameId(), payload.getGameDate());
        } catch (Exception e) {
            log.error("[Kafka] 경기 스케줄 저장 실패 - error: {}", e.getMessage(), e);
            throw new RuntimeException("경기 알림 저장 실패", e);
        }
    }

    @Override
    protected String getConsumerGroupId() {
        return "alarm-service-game-created-consumer";
    }
}
