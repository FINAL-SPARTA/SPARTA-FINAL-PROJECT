package com.fix.event_service.infrastructure.kafka.consumer;

import com.fix.common_service.kafka.consumer.AbstractKafkaConsumer;
import com.fix.common_service.kafka.consumer.RedisIdempotencyChecker;
import com.fix.common_service.kafka.dto.EventKafkaMessage;
import com.fix.common_service.kafka.dto.PointDeductionFailedPayload;
import com.fix.event_service.application.service.EventApplicationService;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class PointDeductionFailedConsumer extends AbstractKafkaConsumer<PointDeductionFailedPayload> {

    private final EventApplicationService eventApplicationService;
    private static final String CONSUMER_GROUP_ID = "event-service-point-deduction-failed-consumer";

    public PointDeductionFailedConsumer(RedisIdempotencyChecker idempotencyChecker,
                                        EventApplicationService eventApplicationService) {
        super(idempotencyChecker);
        this.eventApplicationService = eventApplicationService;
    }

    @KafkaListener(topics = "${kafka-topics.user.point-deduction-failed}", groupId = CONSUMER_GROUP_ID)
    public void listen(ConsumerRecord<String, EventKafkaMessage<PointDeductionFailedPayload>> record,
                       EventKafkaMessage<PointDeductionFailedPayload> message,
                       Acknowledgment ack) {
        super.consume(record, message, ack);
    }

    @Override
    protected void processPayload(PointDeductionFailedPayload payload) {
        log.debug("[Kafka-SAGA] 포인트 차감 실패 이벤트 수신: {}", payload);
        eventApplicationService.cancelEventApply(payload);
    }
}
