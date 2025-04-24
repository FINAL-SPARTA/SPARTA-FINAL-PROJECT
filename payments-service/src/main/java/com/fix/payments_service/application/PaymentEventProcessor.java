package com.fix.payments_service.application;

import com.fix.common_service.kafka.dto.PaymentCompletedPayload;
import com.fix.common_service.kafka.dto.PaymentCompletionFailedPayload;
import com.fix.common_service.kafka.dto.PaymentCancelledPayload;
import com.fix.payments_service.infrastructure.kafka.PaymentProducer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * ✅ 결제 이벤트 발행 전용 서비스
 * - 결제 완료/실패/취소에 따라 Kafka 이벤트를 발행
 * - Feign, 외부 호출 제거
 * - 이벤트 발행 책임만 명확히 유지
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentEventProcessor {

    private final PaymentProducer paymentProducer;

    /**
     * ✅ 결제 완료 이벤트 발행
     */
    public void handlePaymentCompleted(PaymentCompletedPayload payload) {
        try {
            validateCompletedPayload(payload);
            paymentProducer.sendPaymentCompletedEvent(payload);
            log.info("✅ Kafka 발행 완료 - PAYMENT_COMPLETED - orderId={}, ticketCount={}",
                    payload.getOrderId(), payload.getTicketIds().size());
        } catch (Exception e) {
            log.error("❌ Kafka 발행 실패 - PAYMENT_COMPLETED - orderId={}, error={}",
                    payload.getOrderId(), e.getMessage());
            throw e;
        }
    }

    /**
     * ❌ 결제 실패 이벤트 발행
     */
    public void handlePaymentCompletionFailed(PaymentCompletionFailedPayload payload) {
        try {
            paymentProducer.sendPaymentCompletionFailedEvent(payload);
            log.warn("📤 Kafka 발행 완료 - PAYMENT_COMPLETION_FAILED - orderId={}, reason={}",
                    payload.getOrderId(), payload.getFailureReason());
        } catch (Exception e) {
            log.error("❌ Kafka 발행 실패 - PAYMENT_COMPLETION_FAILED - orderId={}, error={}",
                    payload.getOrderId(), e.getMessage());
            throw e;
        }
    }

    /**
     * 🔁 결제 취소 이벤트 발행
     */
    public void handlePaymentCancelled(PaymentCancelledPayload payload) {
        try {
            paymentProducer.sendPaymentCancelledEvent(payload);
            log.info("🔁 Kafka 발행 완료 - PAYMENT_CANCELLED - orderId={}", payload.getOrderId());
        } catch (Exception e) {
            log.error("❌ Kafka 발행 실패 - PAYMENT_CANCELLED - orderId={}, error={}",
                    payload.getOrderId(), e.getMessage());
            throw e;
        }
    }

    /**
     * 필수 필드 검증
     */
    private void validateCompletedPayload(PaymentCompletedPayload payload) {
        if (payload.getOrderId() == null ||
                payload.getPaymentKey() == null ||
                payload.getAmount() <= 0 ||
                payload.getTicketIds() == null || payload.getTicketIds().isEmpty()) {
            throw new IllegalArgumentException("❌ PaymentCompletedPayload 필수 필드 누락");
        }
    }
}

//정상 결제 → 주문 완료
//결제 실패 → 주문 생성 실패
//주문 취소 → 티켓 상태 복원 + 주문 취소