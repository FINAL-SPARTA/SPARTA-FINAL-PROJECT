package com.fix.order_service.infrastructure.kafka;

import com.fix.common_service.dto.OrderCancelledPayload;
import com.fix.common_service.dto.OrderCreatedPayload;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    private static final String ORDER_CREATED_TOPIC = "order-created-topic";
    private static final String ORDER_CANCELLED_TOPIC = "order-cancelled-topic";

    public void sendOrderCreatedEvent(OrderCreatedPayload payload) {
        log.info("📤 Kafka 전송: 주문 생성 이벤트 - {}", payload);
        kafkaTemplate.send(ORDER_CREATED_TOPIC, payload.getOrderId().toString(), payload);
    }

    public void sendOrderCancelledEvent(OrderCancelledPayload payload) {
        log.info("📤 Kafka 전송: 주문 취소 이벤트 - {}", payload);
        kafkaTemplate.send(ORDER_CANCELLED_TOPIC, payload.getOrderId().toString(), payload);
    }
}
