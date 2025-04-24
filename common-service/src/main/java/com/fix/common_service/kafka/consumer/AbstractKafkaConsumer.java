package com.fix.common_service.kafka.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fix.common_service.kafka.dto.EventKafkaMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.messaging.handler.annotation.Payload;

@Slf4j
@RequiredArgsConstructor
public abstract class AbstractKafkaConsumer<T> {

    private final RedisIdempotencyChecker idempotencyChecker;

    public void consume(ConsumerRecord<String, EventKafkaMessage<T>> record,
                        @Payload EventKafkaMessage<T> message,
                        Acknowledgment ack) {
        // 1) Kafka Consumer Group ID 가져오기
        String groupId = getConsumerGroupId();

        // 2) 멱등성 체크를 위한 고유 키 생성 (토픽-그룹ID-파티션-오프셋)
        String messageKey = record.topic() + "-" + groupId + "-" + record.partition() + "-" + record.offset();

        // 3) 멱등성 체크 (이미 처리된 메시지인지 확인)
        if (!idempotencyChecker.isNew(messageKey)) {
            // 이미 처리된 경우, Ack만 하고 로직 종료 (중복 처리 방지)
            if (ack != null) {
                ack.acknowledge();
            }
            return; // 종료
        }

        // 4) 처리 시작 로깅
        log.info("[Kafka] 이벤트 수신 시작. Topic: {}, Key: {}, Partition: {}, Offset: {}, EventType: {}, Payload: {}",
            record.topic(), record.key(), record.partition(), record.offset(), message.getEventType(), message.getPayload());

        // 5) 핵심 비즈니스 로직 처리 위임
        processPayload(message.getPayload());

        // 6) 성공 시 처리 완료 마킹 및 Ack
        idempotencyChecker.markProcessed(messageKey); // 성공 시에만 처리 완료 마킹
        if (ack != null) {
            ack.acknowledge();
        }
        log.info("[Kafka] 이벤트 처리 성공. Key: {}", messageKey);
    }
    protected abstract void processPayload(Object payload);

    protected <T> T mapPayload(Object rawPayload, Class<T> clazz) {
        return new ObjectMapper().convertValue(rawPayload, clazz);
    }

    protected abstract String getConsumerGroupId();
}
