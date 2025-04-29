package com.fix.ticket_service.infrastructure.kafka.consumer;

import com.fix.common_service.kafka.consumer.AbstractKafkaConsumer;
import com.fix.common_service.kafka.consumer.RedisIdempotencyChecker;
import com.fix.common_service.kafka.dto.EventKafkaMessage;
import com.fix.common_service.kafka.dto.OrderCancelledPayload;
import com.fix.ticket_service.application.service.TicketApplicationService;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@Slf4j
public class OrderCanceledConsumer extends AbstractKafkaConsumer<OrderCancelledPayload> {

    private final TicketApplicationService ticketApplicationService;
    private static final String CONSUMER_GROUP_ID = "ticket-service-order-canceled-consumer";

    public OrderCanceledConsumer(RedisIdempotencyChecker idempotencyChecker,
                                 TicketApplicationService ticketApplicationService) {
        super(idempotencyChecker);
        this.ticketApplicationService = ticketApplicationService;
    }

    @KafkaListener(topics = "${kafka-topics.order.canceled}", groupId = CONSUMER_GROUP_ID)
    public void listen(ConsumerRecord<String, EventKafkaMessage<OrderCancelledPayload>> record,
                       EventKafkaMessage<OrderCancelledPayload> message,
                       Acknowledgment ack) {
        super.consume(record, message, ack);
    }

    @Override
    protected void processPayload(Object payload) throws IllegalArgumentException {
        OrderCancelledPayload orderCancelledPayload = mapPayload(payload, OrderCancelledPayload.class);
        UUID orderId = orderCancelledPayload.getOrderId();
        try {
            ticketApplicationService.cancelTicketStatus(orderId);
            log.info("[Kafka] 티켓 취소 상태 업데이트 성공: orderId={}", orderId);
        } catch (IllegalArgumentException e) {
            // 에러 핸들러에 의한 재시도 처리, 보상 트랜잭션은 구현 X
            log.error("[Kafka] 티켓 취소 상태 업데이트 실패: orderId={}, error={}", orderId, e.getMessage());
            throw e;
        }
    }

    @Override
    protected String getConsumerGroupId() {
        return CONSUMER_GROUP_ID;
    }
}
