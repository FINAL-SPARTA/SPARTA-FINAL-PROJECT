package com.fix.user_service.infrastructure.kafka.producer;

import com.fix.common_service.kafka.dto.EventKafkaMessage;
import com.fix.common_service.kafka.dto.PointDeductionFailedPayload;
import com.fix.common_service.kafka.producer.KafkaProducerHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserProducer {

    private final KafkaProducerHelper kafkaProducerHelper;

    @Value("${kafka-topics.user.point-deduction-failed}")
    private String pointDeductionFailedTopic;

    public void sendPointDeductionFailed(UUID eventId, Long userId, UUID eventEntryId, Integer pointsAttempted, String errorCode) {
        PointDeductionFailedPayload payload = new PointDeductionFailedPayload(eventId, userId, eventEntryId, pointsAttempted, errorCode);
        EventKafkaMessage<PointDeductionFailedPayload> message = new EventKafkaMessage<>("POINT_DEDUCTION_FAILED", payload);

        String key = userId.toString();

        kafkaProducerHelper.send(pointDeductionFailedTopic, key, message);
        log.warn("[Kafka-SAGA] 포인트 차감 실패 이벤트 발행: eventId={}, userId={}, eventEntryId={}, pointsAttempted={}, errorCode={}",
            eventId, userId, eventEntryId, pointsAttempted, errorCode);
    }
}
