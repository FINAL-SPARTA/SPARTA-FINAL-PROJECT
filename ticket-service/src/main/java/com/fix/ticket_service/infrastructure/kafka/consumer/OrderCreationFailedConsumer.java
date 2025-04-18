package com.fix.ticket_service.infrastructure.kafka.consumer;

import com.fix.common_service.kafka.consumer.AbstractKafkaConsumer;
import com.fix.common_service.kafka.consumer.RedisIdempotencyChecker;
import com.fix.common_service.kafka.dto.EventKafkaMessage;
import com.fix.common_service.kafka.dto.OrderCreationFailedPayload;
import com.fix.ticket_service.application.service.TicketApplicationService;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class OrderCreationFailedConsumer extends AbstractKafkaConsumer<OrderCreationFailedPayload> {

    private final TicketApplicationService ticketApplicationService;

    public OrderCreationFailedConsumer(RedisIdempotencyChecker idempotencyChecker,
                                       TicketApplicationService ticketApplicationService) {
        super(idempotencyChecker);
        this.ticketApplicationService = ticketApplicationService;
    }

    @KafkaListener(topics = "${kafka-topics.order.creation-failed}", groupId = "ticket-service-order-creation-failed-consumer")
    public void listen(ConsumerRecord<String, EventKafkaMessage<OrderCreationFailedPayload>> record,
                       EventKafkaMessage<OrderCreationFailedPayload> message,
                       Acknowledgment ack) {
        super.consume(record, message, ack);
    }

    @Override
    protected void processPayload(Object rawPayload) throws IllegalArgumentException {
        OrderCreationFailedPayload payload = mapPayload(rawPayload, OrderCreationFailedPayload.class);
        try {
            ticketApplicationService.deleteTickets(payload.getTicketIds());
            log.info("[Kafka] 주문 생성에 실패한 티켓 삭제 성공 : ticketIds={}, failureReason={}",
                payload.getTicketIds(), payload.getFailureReason());
        } catch (IllegalArgumentException e) {
            // 에러 핸들러에 의한 재시도 처리, 보상 트랜잭션은 구현 X
            log.error("[Kafka] 주문 생성에 실패한 티켓 삭제 실패:  error={}", e.getMessage());
        }
    }
}
