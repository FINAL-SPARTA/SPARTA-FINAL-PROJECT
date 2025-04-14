package com.fix.event_service.infrastructure.kafka.producer;

import com.fix.common_service.dto.EventKafkaMessage;
import com.fix.common_service.dto.EventPayload;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventProducer {

    private final KafkaTemplate<String, EventKafkaMessage> kafkaTemplate;

    public void sendEventApplyRequest(UUID eventId, Long userId, Integer requiredPoints) {
        EventPayload payload = new EventPayload(eventId, userId, requiredPoints);

        EventKafkaMessage message = new EventKafkaMessage("EVENT_APPLY_REQUEST", payload);

        kafkaTemplate.send("event-apply-topic", message);
        log.info("[Kafka] EVENT_APPLY_REQUEST 발행: {}", message);
    }
}
