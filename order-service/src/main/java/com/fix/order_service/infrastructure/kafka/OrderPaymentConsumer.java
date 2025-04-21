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
public class OrderPaymentConsumer extends AbstractKafkaConsumer<Object> {

    private final OrderService orderService;
    private final OrderFeignService orderFeignService;

    public OrderPaymentConsumer(RedisIdempotencyChecker idempotencyChecker,
                                OrderService orderService,
                                OrderFeignService orderFeignService) {
        super(idempotencyChecker);
        this.orderService = orderService;
        this.orderFeignService = orderFeignService;
    }

    @Override
    protected void processPayload(Object payload) {
        try {
            if (payload instanceof PaymentSuccessEventPayload successPayload) {
                handlePaymentCompleted(successPayload);
            } else if (payload instanceof OrderCompletionFailedPayload failedPayload) {
                handlePaymentFailed(failedPayload);
            } else if (payload instanceof PaymentCancelledPayload cancelledPayload) {
                handlePaymentCancelled(cancelledPayload);
            } else {
                log.warn("❗️ 알 수 없는 payload 타입 수신됨: {}", payload.getClass().getSimpleName());
            }
        } catch (Exception e) {
            log.error("❌ Kafka 메시지 처리 중 예외 발생: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * ✅ 결제 완료 이벤트 처리
     */
    private void handlePaymentCompleted(PaymentSuccessEventPayload payload) {
        UUID orderId = payload.getOrderId();
        int totalPrice = (int) payload.getAmount(); // 결제 금액
        List<UUID> ticketIds = payload.getTicketIds();
        log.info("✅ [Kafka] 결제 완료 이벤트 수신 - orderId={}, amount={}", orderId, totalPrice);
        orderFeignService.completeOrder(orderId, ticketIds, totalPrice);
    }

    /**
     * ❌ 결제 실패 이벤트 처리
     */
    private void handlePaymentFailed(OrderCompletionFailedPayload payload) {
        UUID orderId = payload.getOrderId();
        log.warn("❌ [Kafka] 결제 실패 이벤트 수신 - orderId={}, reason={}", orderId, payload.getFailureReason());
        orderService.cancelOrderFromPayment(orderId, payload.getFailureReason());
    }

    /**
     * 🔁 결제 취소 이벤트 처리
     */
    private void handlePaymentCancelled(PaymentCancelledPayload payload) {
        UUID orderId = payload.getOrderId();
        log.info("🔁 [Kafka] 결제 취소 이벤트 수신 - orderId={}", orderId);
        orderService.cancelOrderFromPayment(orderId, "사용자 요청에 의한 결제 취소");
    }

    @KafkaListener(topics = "${kafka-topics.payment.completed}", groupId = "order-service")
    public void listenCompleted(ConsumerRecord<String, EventKafkaMessage<Object>> record,
                                @Payload EventKafkaMessage<Object> message,
                                Acknowledgment ack) {
        super.consume(record, message, ack);
    }

    @KafkaListener(topics = "${kafka-topics.payment.completion-failed}", groupId = "order-service")
    public void listenFailed(ConsumerRecord<String, EventKafkaMessage<Object>> record,
                             @Payload EventKafkaMessage<Object> message,
                             Acknowledgment ack) {
        super.consume(record, message, ack);
    }

    @KafkaListener(topics = "${kafka-topics.payment.cancelled}", groupId = "order-service")
    public void listenCancelled(ConsumerRecord<String, EventKafkaMessage<Object>> record,
                                @Payload EventKafkaMessage<Object> message,
                                Acknowledgment ack) {
        super.consume(record, message, ack);
    }
}
