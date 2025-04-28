package com.fix.event_service.infrastructure.kafka.producer;

import com.fix.common_service.kafka.dto.EventApplicationRequestedPayload;
import com.fix.common_service.kafka.dto.EventKafkaMessage;
import com.fix.common_service.kafka.dto.EventWinnersAnnouncedPayload;
import com.fix.common_service.kafka.producer.KafkaProducerHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventProducer {

    private final KafkaProducerHelper kafkaProducerHelper;

    @Value("${kafka-topics.event.applied}")
    private String eventAppliedTopic;

    @Value("${kafka-topics.event.winners-announced}")
    private String eventWinnersAnnouncedTopic;


    public void sendEventApplyRequest(UUID eventId, Long userId, Integer requiredPoints, UUID eventEntryId) {
        EventApplicationRequestedPayload payload =
            new EventApplicationRequestedPayload(eventId, userId, requiredPoints, eventEntryId);
        EventKafkaMessage<EventApplicationRequestedPayload> message =
            new EventKafkaMessage<>("EVENT_APPLY_REQUEST", payload);

        String key = userId.toString();

        kafkaProducerHelper.send(eventAppliedTopic, key, message);
    }

    public void sendEventWinnersNotification(UUID eventId, List<Long> winnerIds) {
        EventWinnersAnnouncedPayload payload =
            new EventWinnersAnnouncedPayload(eventId, winnerIds);
        EventKafkaMessage<EventWinnersAnnouncedPayload> message =
            new EventKafkaMessage<>("EVENT_WINNERS_NOTIFICATION", payload);

        String key = eventId.toString();

        kafkaProducerHelper.send(eventWinnersAnnouncedTopic, key, message);
    }
}
