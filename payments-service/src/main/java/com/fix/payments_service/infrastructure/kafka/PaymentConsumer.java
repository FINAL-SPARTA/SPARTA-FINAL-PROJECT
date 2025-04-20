package com.fix.payments_service.infrastructure.kafka;

import com.fix.common_service.kafka.consumer.AbstractKafkaConsumer;
import com.fix.common_service.kafka.consumer.RedisIdempotencyChecker;
import com.fix.common_service.kafka.dto.*;
import com.fix.payments_service.application.PaymentEventProcessor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

/**
 * ✅ Payment 관련 Kafka 이벤트를 수신하는 Consumer 클래스
 * - 결제 성공, 실패, 취소 이벤트를 하나의 클래스에서 처리
 * - 멱등성 체크, 서비스 계층 호출 포함
 */
@Slf4j
@Component

public class PaymentConsumer extends AbstractKafkaConsumer<Object> {

    private final PaymentEventProcessor paymentEventProcessor;

    public PaymentConsumer(RedisIdempotencyChecker idempotencyChecker,
                           PaymentEventProcessor paymentEventProcessor) {
        super(idempotencyChecker);
        this.paymentEventProcessor = paymentEventProcessor;
    }

    @Override
    protected void processPayload(Object payload) {
        // 이벤트 타입에 따라 payload를 매핑 후 처리
        if (payload instanceof PaymentCompletedPayload completedPayload) {
            handlePaymentCompleted(completedPayload);
        } else if (payload instanceof PaymentCompletionFailedPayload failedPayload) {
            handlePaymentCompletionFailed(failedPayload);
        } else if (payload instanceof PaymentCancelledPayload cancelledPayload) {
            handlePaymentCancelled(cancelledPayload);
        } else {
            log.warn("❗ 알 수 없는 페이로드 타입 수신: {}", payload.getClass().getSimpleName());
        }
    }

    /**
     * ✅ 결제 완료 이벤트 처리
     * 주문 상태를 완료로 변경 요청
     */
    private void handlePaymentCompleted(PaymentCompletedPayload payload) {
        log.info("✅ [Kafka] 결제 완료 이벤트 수신 - orderId={}", payload.getOrderId());
        paymentEventProcessor.handlePaymentCompleted(payload);
    }

    /**
     * ❌ 결제 실패 이벤트 처리
     * 주문을 실패 상태로 변경 요청
     */
    private void handlePaymentCompletionFailed(PaymentCompletionFailedPayload payload) {
        log.warn("❌ [Kafka] 결제 실패 이벤트 수신 - orderId={}, reason={}",
                payload.getOrderId(), payload.getFailureReason());
        paymentEventProcessor.handlePaymentCompletionFailed(payload);
    }

    /**
     * 🔁 결제 취소(환불) 이벤트 처리
     * 주문을 취소 상태로 변경 요청
     */
    private void handlePaymentCancelled(PaymentCancelledPayload payload) {
        log.info("🔁 [Kafka] 결제 취소 이벤트 수신 - orderId={}", payload.getOrderId());
        paymentEventProcessor.handlePaymentCancelled(payload);
    }

    /**
     * Kafka 리스너 - 결제 완료 이벤트
     */
    @KafkaListener(topics = "${kafka-topics.payment.completed}", groupId = "payment-service")
    public void listenCompleted(ConsumerRecord<String, EventKafkaMessage<Object>> record,
                                @Payload EventKafkaMessage<Object> message,
                                Acknowledgment ack) {
        super.consume(record, message, ack);
    }

    /**
     * Kafka 리스너 - 결제 실패 이벤트
     */
    @KafkaListener(topics = "${kafka-topics.payment.completion-failed}", groupId = "payment-service")
    public void listenFailed(ConsumerRecord<String, EventKafkaMessage<Object>> record,
                             @Payload EventKafkaMessage<Object> message,
                             Acknowledgment ack) {
        super.consume(record, message, ack);
    }

    /**
     * Kafka 리스너 - 결제 취소 이벤트
     */
    @KafkaListener(topics = "${kafka-topics.payment.cancelled}", groupId = "payment-service")
    public void listenCancelled(ConsumerRecord<String, EventKafkaMessage<Object>> record,
                                @Payload EventKafkaMessage<Object> message,
                                Acknowledgment ack) {
        super.consume(record, message, ack);
    }
}