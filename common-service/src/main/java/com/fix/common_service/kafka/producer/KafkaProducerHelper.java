package com.fix.common_service.kafka.producer;

import com.fix.common_service.kafka.dto.EventKafkaMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Component
@RequiredArgsConstructor
@Slf4j
public class KafkaProducerHelper {
    private final KafkaTemplate<String, EventKafkaMessage<?>> kafkaTemplate;

    public <T> void send(String topic, String key, EventKafkaMessage<T> message) {
        T payload = message.getPayload();

        CompletableFuture<SendResult<String, EventKafkaMessage<?>>> future = kafkaTemplate.send(topic, key, message);

        future.whenComplete((result, throwable) -> {
            if  (throwable == null) {
                // 성공 시 로직
                log.info("[Kafka] 이벤트 발행 성공. Topic : {}, Key : {}, Partition : {}," +
                        " Offset : {}, EventType : {}, Payload : {}",
                        topic, key, result.getRecordMetadata().partition(), result.getRecordMetadata().offset(),
                    message.getEventType(), payload);
            } else {
                // 실패 시 로직
                log.error("[Kafka] 이벤트 발행 실패. Topic : {}, Key : {}, EventType : {}, Payload : {}, Reason: {}",
                        topic, key, message.getEventType(), payload, throwable.getMessage(), throwable);
            }
        });
    }

}
