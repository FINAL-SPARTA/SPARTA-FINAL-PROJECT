package com.fix.alarm_service.infrastructure.kafka.consumer;


import com.fix.alarm_service.application.dtos.response.PhoneNumberResponseDto;
import com.fix.alarm_service.application.service.AlarmService;
import com.fix.common_service.kafka.consumer.AbstractKafkaConsumer;
import com.fix.common_service.kafka.consumer.RedisIdempotencyChecker;
import com.fix.common_service.kafka.dto.EventKafkaMessage;
import com.fix.common_service.kafka.dto.TicketReservedPayload;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;
@Slf4j
@Component
public class TicketReservedConsumer extends AbstractKafkaConsumer<TicketReservedPayload> {
    private final AlarmService alarmService;
    public TicketReservedConsumer(RedisIdempotencyChecker idempotencyChecker, AlarmService alarmService) {
        super(idempotencyChecker);
        this.alarmService = alarmService;
    }
    @KafkaListener(
            topics = "${kafka-topics.ticket.reserved}",
            groupId = "alarm-service-ticket-reserved-consumer"
    )
    public void listen(ConsumerRecord<String, EventKafkaMessage<TicketReservedPayload>> record,
                       EventKafkaMessage<TicketReservedPayload> message,
                       Acknowledgment ack) {
        super.consume(record, message, ack);
    }
    @Override
    protected void processPayload(Object rawPayload) {
        TicketReservedPayload payload = mapPayload(rawPayload, TicketReservedPayload.class);
        try {
            // 유저 전화번호 조회
            PhoneNumberResponseDto phone = alarmService.getPhoneNumber(payload.getUserId());
            // 알림 메시지 생성
            String message = String.format(
                    "[예매 완료]  티켓이 성공적으로 예매되었습니다. 총 %d매.",
                    payload.getTicketDetails().size()
            );
            alarmService.sendSns(phone.getPhoneNumber(), message);
            log.info("[Kafka][Alarm] 유저 {} 에게 예매 완료 알림 전송 성공", payload.getUserId());
        } catch (Exception e) {
            log.error("[Kafka][Alarm] 예매 완료 알림 전송 실패 - userId: {}, error: {}", payload.getUserId(), e.getMessage());
            throw new RuntimeException("예매 완료 알림 실패");
        }
    }
}


















