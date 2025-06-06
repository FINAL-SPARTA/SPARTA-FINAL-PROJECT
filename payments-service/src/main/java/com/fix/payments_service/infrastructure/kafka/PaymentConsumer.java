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

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * ✅ Payment 관련 Kafka 이벤트를 수신하는 Consumer 클래스
 * - 결제 성공, 실패, 취소 이벤트를 하나의 클래스에서 처리
 * - 멱등성 체크, 서비스 계층 호출 포함
 */
@Slf4j
@Component
public class PaymentConsumer {

    // ✅ ticketIds를 orderId 기준으로 보관하는 캐시
    private static final Map<UUID, List<UUID>> ticketIdCache = new ConcurrentHashMap<>();

    public static void cacheTicketIds(UUID orderId, List<UUID> ticketIds) {
        ticketIdCache.put(orderId, ticketIds);
    }

    public static List<UUID> getTicketIds(UUID orderId) {
        return ticketIdCache.get(orderId);
    }

    public static void removeTicketIds(UUID orderId) {
        ticketIdCache.remove(orderId);
    }

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

    @KafkaListener(topics = "${kafka-topics.order.created}", containerFactory = "kafkaListenerContainerFactory",
            groupId = "payment-service-order-created-consumer")
    public void consumeOrderCreated(
            ConsumerRecord<String, EventKafkaMessage<OrderCreatedPayload>> record,
            @Payload EventKafkaMessage<OrderCreatedPayload> message,
            Acknowledgment ack) {
        orderCreatedEventConsumer.consume(record, message, ack);
    }

    @KafkaListener(topics = "${kafka-topics.order.canceled}", containerFactory = "kafkaListenerContainerFactory",
            groupId = "payment-service-order-cancelled-consumer")
    public void consumeOrderCancelled(
            ConsumerRecord<String, EventKafkaMessage<OrderCancelledPayload>> record,
            @Payload EventKafkaMessage<OrderCancelledPayload> message,
            Acknowledgment ack) {
        orderCancelledEventConsumer.consume(record, message, ack);
    }

    @KafkaListener(topics = "${kafka-topics.order.completed}", containerFactory = "kafkaListenerContainerFactory",
            groupId = "payment-service-order-completed-consumer")
    public void consumeOrderCompleted(
            ConsumerRecord<String, EventKafkaMessage<OrderCompletedPayload>> record,
            @Payload EventKafkaMessage<OrderCompletedPayload> message,
            Acknowledgment ack) {
        orderCompletedEventConsumer.consume(record, message, ack);
    }

    @KafkaListener(topics = "${kafka-topics.order.completion-failed}", containerFactory = "kafkaListenerContainerFactory",
            groupId = "payment-service-order-completion-failed-consumer")
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

            // ✅ ticketIds 캐시 저장
            cacheTicketIds(order.getOrderId(), order.getTicketIds());
            paymentEventService.processPaymentRequest(order);
        }

        @Override
        protected String getConsumerGroupId() {
            return "payment-service-order-created-consumer";
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

        @Override
        protected String getConsumerGroupId() {
            return "payment-service-order-cancelled-consumer";
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

        @Override
        protected String getConsumerGroupId() {
            return "payment-service-order-completed-consumer";
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

        @Override
        protected String getConsumerGroupId() {
            return "payment-service-order-completion-failed-consumer";
        }
    }
}