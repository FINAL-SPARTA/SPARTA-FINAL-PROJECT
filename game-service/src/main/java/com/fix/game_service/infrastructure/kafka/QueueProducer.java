package com.fix.game_service.infrastructure.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class QueueProducer {

    private final KafkaTemplate<String, String> kafkaQueueTemplate;
    private static final String ENTER_QUEUE_TOPIC = "enter-queue-topic";

    public void sendEnterQueue(UUID gameId, String token, Long userId, long timestamp) {
        String key = gameId.toString();
        String value = token + "|" + userId + "/" + timestamp;

        try {
            kafkaQueueTemplate.send(ENTER_QUEUE_TOPIC, key, value);
            log.info("Kafka 전송 완료: topic={}, key={}, value={}", ENTER_QUEUE_TOPIC, key, value);
        } catch (Exception e) {
            log.error("Kafka 전송 실패: {}", e.getMessage(), e);
            throw new RuntimeException("Kafka 메시지 전송 실패", e);
        }
    }

}
