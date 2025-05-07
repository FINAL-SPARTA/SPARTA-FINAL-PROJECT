package com.fix.game_service.infrastructure.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class QueueConsumer {

    private final RedisTemplate<String, String> redisTemplate;

    private static final String ENTER_QUEUE_TOPIC = "enter-queue-topic";

    private final String QUEUE_KEY_PREFIX = "queue:";

    @KafkaListener(topics = ENTER_QUEUE_TOPIC, groupId = "queue-game-group",
            containerFactory = "kafkaListenerContainerQueueFactory")
    public void listenEnterQueue(ConsumerRecord<String, String> record) {
        try {
            String gameId = record.key();
            String value = record.value();

            String[] parts = value.split("/");
            String tokenAndUserId = parts[0];
            long timestamp = Long.parseLong(parts[1]);

            String queueKey = QUEUE_KEY_PREFIX + gameId;

            redisTemplate.opsForZSet().add(queueKey, tokenAndUserId, timestamp);

            log.info("Kafka 메시지 소비 완료: gameId={}, tokenAneUserId={}, timestamp={}",
                    gameId, tokenAndUserId, timestamp);
        } catch (Exception e) {
            log.error("Kafka 메시지 처리 중 오류 발생: {}", e.getMessage(), e);
        }
    }


}
