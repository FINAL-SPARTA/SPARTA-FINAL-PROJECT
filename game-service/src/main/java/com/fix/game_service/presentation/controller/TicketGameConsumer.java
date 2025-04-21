package com.fix.game_service.presentation.controller;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;

import com.fix.common_service.kafka.consumer.AbstractKafkaConsumer;
import com.fix.common_service.kafka.consumer.RedisIdempotencyChecker;
import com.fix.common_service.kafka.dto.EventKafkaMessage;
import com.fix.common_service.kafka.dto.TicketUpdatedPayload;
import com.fix.game_service.application.service.ConsumerService;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class TicketGameConsumer extends AbstractKafkaConsumer<TicketUpdatedPayload> {

    private final ConsumerService consumerService;


    public TicketGameConsumer(RedisIdempotencyChecker idempotencyChecker,
                              ConsumerService consumerService) {
        super(idempotencyChecker);
        this.consumerService = consumerService;

    }

    /**
     * Kafka에서 메시지 소비를 위한 리스너 메서드 정의
     *
     * @param message : 이벤트에서 수신할 데이터
     */
    @KafkaListener(topics = "ticket-updated-topic", groupId = "game-service-group")
    public void updateGameSeatsByConsumer(ConsumerRecord<String, EventKafkaMessage<TicketUpdatedPayload>> record,
                                          EventKafkaMessage<TicketUpdatedPayload> message,
                                          Acknowledgment acknowledgment) {
        log.info("[Kafka] 티켓 이벤트 수신 : {}", message.getEventType());
        super.consume(record, message, acknowledgment);
    }

    @Override
    protected void processPayload(Object rawPayload) {
        TicketUpdatedPayload payload = mapPayload(rawPayload, TicketUpdatedPayload.class);

        consumerService.updateGameSeatsByConsumer(payload);

    }
}





