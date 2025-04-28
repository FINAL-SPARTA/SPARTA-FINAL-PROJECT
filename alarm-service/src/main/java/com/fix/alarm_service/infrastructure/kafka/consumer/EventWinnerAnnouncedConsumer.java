package com.fix.alarm_service.infrastructure.kafka.consumer;

import com.fix.alarm_service.application.dtos.response.PhoneNumberResponseDto;
import com.fix.alarm_service.application.service.AlarmService;
import com.fix.common_service.kafka.consumer.AbstractKafkaConsumer;
import com.fix.common_service.kafka.consumer.RedisIdempotencyChecker;
import com.fix.common_service.kafka.dto.EventKafkaMessage;
import com.fix.common_service.kafka.dto.EventWinnersAnnouncedPayload;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Optional;

@Slf4j
@Component
public class EventWinnerAnnouncedConsumer extends AbstractKafkaConsumer<EventWinnersAnnouncedPayload> {

    private final AlarmService alarmService;


    public EventWinnerAnnouncedConsumer(RedisIdempotencyChecker idempotencyChecker, AlarmService alarmService) {
        super(idempotencyChecker);
        this.alarmService = alarmService;
    }

    @KafkaListener(
            topics = "${kafka-topics.event.winners-announced}",
            groupId = "alarm-service-event-winners-announced-consumer"
    )

    public void listen(ConsumerRecord<String, EventKafkaMessage<EventWinnersAnnouncedPayload>> record,
                       EventKafkaMessage<EventWinnersAnnouncedPayload> message,
                       Acknowledgment ack) {
        super.consume(record, message, ack);
    }


    @Override
    protected void processPayload(Object rawPayload) {
        EventWinnersAnnouncedPayload payload = mapPayload(rawPayload, EventWinnersAnnouncedPayload.class);
        Optional.ofNullable(payload.getWinnerIds())
                .orElse(Collections.emptyList())
                .forEach(winnerId -> {
                    try {
                        PhoneNumberResponseDto phone = alarmService.getPhoneNumber(winnerId);
                        String message = String.format(
                                "[이벤트 당첨] 축하합니다! 이벤트 %s에 당첨되셨습니다.",
                                payload.getEventId().toString()
                        );
                        alarmService.sendSns(phone.getPhoneNumber(), message);
                        log.info("[Kafka][Alarm] 수신자 {} 에게 당첨 알림 전송 성공", winnerId);
                    } catch (Exception e) {
                        log.error("[Kafka][Alarm] 수신자 {} 에게 당첨 알림 전송 실패 :{}", winnerId, e.getMessage());
                    }
                });
    }

    @Override
    protected String getConsumerGroupId() {
        return "alarm-service-event-winners-announced-consumer";
    }
}