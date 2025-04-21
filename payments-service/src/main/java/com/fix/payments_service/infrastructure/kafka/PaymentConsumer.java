package com.fix.payments_service.infrastructure.kafka;

import com.fix.common_service.kafka.consumer.AbstractKafkaConsumer;
import com.fix.common_service.kafka.consumer.RedisIdempotencyChecker;
import com.fix.common_service.kafka.dto.*;
import com.fix.payments_service.application.PaymentEventService;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class PaymentConsumer {

    private final OrderCreatedEventConsumer orderCreatedEventConsumer;
    private final OrderCancelledEventConsumer orderCancelledEventConsumer;
    private final OrderCompletedEventConsumer orderCompletedEventConsumer;
    private final OrderCompletionFailedEventConsumer orderCompletionFailedEventConsumer;

    public PaymentConsumer(RedisIdempotencyChecker idempotencyChecker,
                           PaymentEventService paymentEventService) {
        this.orderCreatedEventConsumer = new OrderCreatedEventConsumer(idempotencyChecker, paymentEventService);
        this.orderCancelledEventConsumer = new OrderCancelledEventConsumer(idempotencyChecker, paymentEventService);
        this.orderCompletedEventConsumer = new OrderCompletedEventConsumer(idempotencyChecker);
        this.orderCompletionFailedEventConsumer = new OrderCompletionFailedEventConsumer(idempotencyChecker);
    }

    @KafkaListener(topics = "${kafka-topics.order.created}", containerFactory = "kafkaListenerContainerFactory")
    public void consumeOrderCreated(
            ConsumerRecord<String, EventKafkaMessage<OrderCreatedPayload>> record,
            @Payload EventKafkaMessage<OrderCreatedPayload> message,
            Acknowledgment ack) {
        orderCreatedEventConsumer.consume(record, message, ack);
    }

    @KafkaListener(topics = "${kafka-topics.order.canceled}", containerFactory = "kafkaListenerContainerFactory")
    public void consumeOrderCancelled(
            ConsumerRecord<String, EventKafkaMessage<OrderCancelledPayload>> record,
            @Payload EventKafkaMessage<OrderCancelledPayload> message,
            Acknowledgment ack) {
        orderCancelledEventConsumer.consume(record, message, ack);
    }

    @KafkaListener(topics = "${kafka-topics.order.completed}", containerFactory = "kafkaListenerContainerFactory")
    public void consumeOrderCompleted(
            ConsumerRecord<String, EventKafkaMessage<OrderCompletedPayload>> record,
            @Payload EventKafkaMessage<OrderCompletedPayload> message,
            Acknowledgment ack) {
        orderCompletedEventConsumer.consume(record, message, ack);
    }

    @KafkaListener(topics = "${kafka-topics.order.completion-failed}", containerFactory = "kafkaListenerContainerFactory")
    public void consumeOrderCompletionFailed(
            ConsumerRecord<String, EventKafkaMessage<OrderCompletionFailedPayload>> record,
            @Payload EventKafkaMessage<OrderCompletionFailedPayload> message,
            Acknowledgment ack) {
        orderCompletionFailedEventConsumer.consume(record, message, ack);
    }

    /**
     * 주문 생성 → 결제 시도
     */
    static class OrderCreatedEventConsumer extends AbstractKafkaConsumer<OrderCreatedPayload> {
        private final PaymentEventService paymentEventService;

        public OrderCreatedEventConsumer(RedisIdempotencyChecker checker, PaymentEventService service) {
            super(checker);
            this.paymentEventService = service;
        }

        @Override
        protected void processPayload(Object payload) {
            OrderCreatedPayload order = mapPayload(payload, OrderCreatedPayload.class);
            log.info("[Kafka] ORDER_CREATED 수신 → 결제 시도 시작: orderId={}", order.getOrderId());
            paymentEventService.processPaymentRequest(order);
        }
    }

    /**
     * 주문 취소 → 결제 취소
     */
    static class OrderCancelledEventConsumer extends AbstractKafkaConsumer<OrderCancelledPayload> {
        private final PaymentEventService paymentEventService;

        public OrderCancelledEventConsumer(RedisIdempotencyChecker checker, PaymentEventService service) {
            super(checker);
            this.paymentEventService = service;
        }

        @Override
        protected void processPayload(Object payload) {
            OrderCancelledPayload cancelled = mapPayload(payload, OrderCancelledPayload.class);
            log.info("[Kafka] ORDER_CANCELED 수신 → 결제 취소 시작: orderId={}", cancelled.getOrderId());
            paymentEventService.sendPaymentCancelled(cancelled.getOrderId());
        }
    }

    /**
     * 주문 완료 → 로그만 기록 (결제 완료는 다른 흐름에서 처리됨)
     */
    static class OrderCompletedEventConsumer extends AbstractKafkaConsumer<OrderCompletedPayload> {

        public OrderCompletedEventConsumer(RedisIdempotencyChecker checker) {
            super(checker);
        }

        @Override
        protected void processPayload(Object payload) {
            OrderCompletedPayload completed = mapPayload(payload, OrderCompletedPayload.class);
            log.info("[Kafka] ORDER_COMPLETED 수신: orderId={}", completed.getOrderId());
        }
    }

    /**
     * 주문 완료 실패 → 추후 처리 확장 가능
     */
    static class OrderCompletionFailedEventConsumer extends AbstractKafkaConsumer<OrderCompletionFailedPayload> {
        public OrderCompletionFailedEventConsumer(RedisIdempotencyChecker checker) {
            super(checker);
        }

        @Override
        protected void processPayload(Object payload) {
            OrderCompletionFailedPayload failed = mapPayload(payload, OrderCompletionFailedPayload.class);
            log.warn("[Kafka] ORDER_COMPLETION_FAILED 수신: orderId={}, reason={}", failed.getOrderId(), failed.getFailureReason());
        }
    }
}
