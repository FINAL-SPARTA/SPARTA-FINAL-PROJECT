package com.fix.user_service.infrastructure.kafka.consumer;


import com.fix.common_service.kafka.consumer.AbstractKafkaConsumer;
import com.fix.common_service.kafka.consumer.RedisIdempotencyChecker;
import com.fix.common_service.kafka.dto.EventApplicationRequestedPayload;
import com.fix.common_service.kafka.dto.EventKafkaMessage;
import com.fix.user_service.application.exception.UserException;
import com.fix.user_service.application.service.UserService;
import com.fix.user_service.infrastructure.kafka.producer.UserProducer;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class EventApplicationConsumer extends AbstractKafkaConsumer<EventApplicationRequestedPayload> {

    private final UserService userService;
    private final UserProducer userProducer;

    public EventApplicationConsumer(RedisIdempotencyChecker idempotencyChecker, UserService userService, UserProducer userProducer) {
        super(idempotencyChecker);
        this.userService = userService;
        this.userProducer = userProducer;
    }

    @KafkaListener(topics = "${kafka-topics.event.applied}", groupId = "user-service-event-applied-consumer")
    public void listen(ConsumerRecord<String, EventKafkaMessage<EventApplicationRequestedPayload>> record,
                       EventKafkaMessage<EventApplicationRequestedPayload> message,
                       Acknowledgment ack) {
        super.consume(record, message, ack);
    }

    @Override
    protected void processPayload(Object rawPayload) throws UserException {
        EventApplicationRequestedPayload payload = mapPayload(rawPayload, EventApplicationRequestedPayload.class);
        try {
            userService.deductPoints(payload.getUserId(), payload.getPoints());
            log.info("[Kafka] 포인트 차감 성공: userId={}, points={}", payload.getUserId(), payload.getPoints());
        } catch (UserException e) {
            log.warn("[Kafka-SAGA] 포인트 차감 실패: eventId={}, userId={}, points={}, error={}",
                payload.getEventId(), payload.getUserId(), payload.getPoints(), e.getMessage());

            userProducer.sendPointDeductionFailed(payload.getEventId(), payload.getUserId(), payload.getEventEntryId(),
                payload.getPoints(), e.getErrorCode());
        }
    }

    @Override
    protected String getConsumerGroupId() {
        return "user-service-event-applied-consumer";
    }
}
