package com.fix.order_service.infrastructure.kafka;

import com.fix.common_service.kafka.consumer.AbstractKafkaConsumer;
import com.fix.common_service.kafka.consumer.RedisIdempotencyChecker;
import com.fix.common_service.kafka.dto.*;
import com.fix.order_service.application.OrderFeignService;
import com.fix.order_service.application.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/**
 * ✅ 결제 결과에 따른 주문 상태 처리를 담당하는 Kafka Consumer
 * - 결제 완료 → 주문 완료 처리
 * - 결제 실패/취소 → 주문 취소 처리
 */
@Slf4j
@Component
public class OrderPaymentConsumer {

    private final PaymentCompletedConsumer completedConsumer;
    private final CompletionFailedConsumer failedConsumer;
    private final PaymentCancelledConsumer cancelledConsumer;

    public OrderPaymentConsumer(RedisIdempotencyChecker idempotencyChecker,
                                OrderService orderService,
                                OrderFeignService orderFeignService) {
        this.completedConsumer = new PaymentCompletedConsumer(idempotencyChecker, orderFeignService);
        this.failedConsumer = new CompletionFailedConsumer(idempotencyChecker, orderService);
        this.cancelledConsumer = new PaymentCancelledConsumer(idempotencyChecker, orderService);
    }

    @KafkaListener(topics = "${kafka-topics.payment.completed}", containerFactory = "kafkaListenerContainerFactory",
            groupId = "order-service-payment-completed-consumer")
    public void consumePaymentCompleted(
            ConsumerRecord<String, EventKafkaMessage<PaymentCompletedPayload>> record,
            @Payload EventKafkaMessage<PaymentCompletedPayload> message,
            Acknowledgment ack
    ) {
        completedConsumer.consume(record, message, ack);
    }

    @KafkaListener(topics = "${kafka-topics.payment.completion-failed}", containerFactory = "kafkaListenerContainerFactory",
            groupId = "order-service-payment-completion-failed-consumer")
    public void consumeCompletionFailed(
            ConsumerRecord<String, EventKafkaMessage<OrderCompletionFailedPayload>> record,
            @Payload EventKafkaMessage<OrderCompletionFailedPayload> message,
            Acknowledgment ack
    ) {
        failedConsumer.consume(record, message, ack);
    }

    @KafkaListener(
            topics = "${kafka-topics.payment.cancelled}",
            containerFactory = "kafkaListenerContainerFactory",
            groupId = "order-service-payment-cancelled-consumer"
    )
    public void consumePaymentCancelled(
            ConsumerRecord<String, EventKafkaMessage<PaymentCancelledPayload>> record,
            @Payload EventKafkaMessage<PaymentCancelledPayload> message,
            Acknowledgment ack
    ) {
        cancelledConsumer.consume(record, message, ack);
    }

    /**
     * 결제 완료 이벤트 처리
     */
    static class PaymentCompletedConsumer extends AbstractKafkaConsumer<PaymentCompletedPayload> {
        private final OrderFeignService orderFeignService;

        public PaymentCompletedConsumer(RedisIdempotencyChecker idempotencyChecker,
                                        OrderFeignService orderFeignService) {
            super(idempotencyChecker);
            this.orderFeignService = orderFeignService;
        }

        @Override
        protected void processPayload(Object raw) {
            PaymentCompletedPayload payload = mapPayload(raw, PaymentCompletedPayload.class);

            log.info("[Kafka] 결제 완료 이벤트 처리 - orderId={}, amount={}",
                    payload.getOrderId(), payload.getAmount());

            // 주문 상태 COMPLETED로 전환 + 후속 처리 (Feign 호출)
            orderFeignService.completeOrder(
                    payload.getOrderId(),
                    payload.getTicketIds(),
                    (int) payload.getAmount()
            );
        }

        @Override
        protected String getConsumerGroupId() {
            return "order-service-payment-completed-consumer";
        }
    }

    /**
     * 결제 실패 이벤트 처리
     */
    static class CompletionFailedConsumer extends AbstractKafkaConsumer<OrderCompletionFailedPayload> {
        private final OrderService orderService;

        public CompletionFailedConsumer(RedisIdempotencyChecker idempotencyChecker,
                                        OrderService orderService) {
            super(idempotencyChecker);
            this.orderService = orderService;
        }

        @Override
        protected void processPayload(Object raw) {
            OrderCompletionFailedPayload payload = mapPayload(raw, OrderCompletionFailedPayload.class);

            log.warn("[Kafka] 결제 실패 이벤트 처리 - orderId={}, reason={}",
                    payload.getOrderId(), payload.getFailureReason());

            // 주문 취소 처리
            orderService.cancelOrderFromPayment(
                    payload.getOrderId(),
                    payload.getFailureReason()
            );
        }

        @Override
        protected String getConsumerGroupId() {
            return "order-service-payment-completion-failed-consumer";
        }
    }

    /**
     * 결제 취소 이벤트 처리
     */
    static class PaymentCancelledConsumer extends AbstractKafkaConsumer<PaymentCancelledPayload> {
        private final OrderService orderService;

        public PaymentCancelledConsumer(RedisIdempotencyChecker idempotencyChecker,
                                        OrderService orderService) {
            super(idempotencyChecker);
            this.orderService = orderService;
        }

        @Override
        protected void processPayload(Object raw) {
            PaymentCancelledPayload payload = mapPayload(raw, PaymentCancelledPayload.class);

            log.info("[Kafka] 결제 취소 이벤트 처리 - orderId={}", payload.getOrderId());

            // 주문 취소 처리 (사용자 요청)
            orderService.cancelOrderFromPayment(
                    payload.getOrderId(),
                    "사용자 요청에 의한 결제 취소"
            );
        }

        @Override
        protected String getConsumerGroupId() {
            return "order-service-payment-cancelled-consumer";
        }
    }
}