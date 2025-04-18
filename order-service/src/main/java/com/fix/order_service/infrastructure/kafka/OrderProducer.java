package com.fix.order_service.infrastructure.kafka;

import com.fix.common_service.kafka.dto.*;
import com.fix.common_service.kafka.producer.KafkaProducerHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderProducer {

    private final KafkaProducerHelper kafkaProducerHelper;

    @Value("${kafka-topics.order.created}")
    private String orderCreatedTopic;

    @Value("${kafka-topics.order.creation-failed}")
    private String orderCreationFailedTopic;

    @Value("${kafka-topics.order.completed}")
    private String orderCompletedTopic;

    @Value("${kafka-topics.order.completion-failed}")
    private String orderCompletionFailedTopic;

    @Value("${kafka-topics.order.canceled}")
    private String orderCanceledTopic;

    public void sendOrderCreatedEvent(String orderId, OrderCreatedPayload payload) {
        send(orderCreatedTopic, orderId, "ORDER_CREATED", payload);
    }

    public void sendOrderCreationFailedEvent(String orderId, OrderFailedPayload payload) {
        send(orderCreationFailedTopic, orderId, "ORDER_CREATION_FAILED", payload);
    }

    public void sendOrderCompletedEvent(String orderId, OrderCompletedPayload payload) {
        send(orderCompletedTopic, orderId, "ORDER_COMPLETED", payload);
    }

    public void sendOrderCompletionFailedEvent(String orderId, OrderFailedPayload payload) {
        send(orderCompletionFailedTopic, orderId, "ORDER_COMPLETION_FAILED", payload);
    }

    public void sendOrderCancelledEvent(String orderId, OrderCancelledPayload payload) {
        send(orderCanceledTopic, orderId, "ORDER_CANCELED", payload);
    }

    private <T> void send(String topic, String key, String type, T payload) {
        EventKafkaMessage<T> message = new EventKafkaMessage<>(type, payload);
        kafkaProducerHelper.send(topic, key, message);
    }
}
