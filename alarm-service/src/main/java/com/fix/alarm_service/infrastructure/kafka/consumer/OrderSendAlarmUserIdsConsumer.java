package com.fix.alarm_service.infrastructure.kafka.consumer;

import com.fix.alarm_service.application.dtos.response.PhoneNumberResponseDto;
import com.fix.alarm_service.application.service.AlarmService;
import com.fix.common_service.kafka.consumer.AbstractKafkaConsumer;
import com.fix.common_service.kafka.consumer.RedisIdempotencyChecker;
import com.fix.common_service.kafka.dto.EventKafkaMessage;
import com.fix.common_service.kafka.dto.OrderSendAlarmUserIdsPayload;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Optional;


@Slf4j
@Component
public class OrderSendAlarmUserIdsConsumer extends AbstractKafkaConsumer<OrderSendAlarmUserIdsPayload> {

    private final AlarmService alarmService;


    public OrderSendAlarmUserIdsConsumer(RedisIdempotencyChecker idempotencyChecker, AlarmService alarmService) {
        super(idempotencyChecker);
        this.alarmService = alarmService;
    }

    @KafkaListener(
            topics = "${kafka-topics.order.send-alarm-userIds}",
            groupId = "alarm-service-order-send-alarm-userIds-consumer"
    )

    public void listen(ConsumerRecord<String, EventKafkaMessage<OrderSendAlarmUserIdsPayload>> record,
                       EventKafkaMessage<OrderSendAlarmUserIdsPayload> message,
                       Acknowledgment ack) {
        super.consume(record, message, ack);
    }


    @Override
    protected void processPayload(Object rawPayload) {

        OrderSendAlarmUserIdsPayload payload = mapPayload(rawPayload,  OrderSendAlarmUserIdsPayload.class);
        Optional.ofNullable(payload.getUserIds())
                .orElse(Collections.emptyList())
                .forEach(userId -> {
                    try {
                        PhoneNumberResponseDto phone = alarmService.getPhoneNumber(userId);
                        String message = String.format(
                                "[경기 알림] 내일은 예약하신 %s 경기가 열립니다.",
                                payload.getGameId().toString()
                        );
                        alarmService.sendSns(phone.getPhoneNumber(), message);
                        log.info("[Kafka][Alarm] 수신자 {} 에게 당첨 알림 전송 성공", userId);
                    } catch (Exception e) {
                        log.error("[Kafka][Alarm] 수신자 {} 에게 당첨 알림 전송 실패 :{}", userId, e.getMessage());
                    }
                });



    }

    @Override
    protected String getConsumerGroupId() {
        return "alarm-service-order-send-alarm-userIds-consumer";
    }
}
