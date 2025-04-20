package com.fix.payments_service.application;

import com.fix.common_service.kafka.dto.PaymentCompletedPayload;
import com.fix.common_service.kafka.dto.PaymentCompletionFailedPayload;
import com.fix.common_service.kafka.dto.PaymentCancelledPayload;
import com.fix.payments_service.infrastructure.client.OrderServiceClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * ✅ Kafka 결제 이벤트 수신 후 실제 주문 상태를 변경하는 서비스 계층
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentEventProcessor {

    private final OrderServiceClient orderServiceClient;

    /**
     * ✅ 결제 완료 이벤트 처리
     * 주문을 완료 상태로 변경 요청
     */
    public void handlePaymentCompleted(PaymentCompletedPayload payload) {
        UUID orderId = payload.getOrderId();
        log.info("✅ 주문 완료 처리 요청 - orderId={}", orderId);
        try {
            orderServiceClient.completeOrder(orderId.toString());
        } catch (Exception e) {
            log.error("❌ 주문 완료 처리 실패 - orderId={}, error={}", orderId, e.getMessage());
        }
    }

    /**
     * ❌ 결제 실패 이벤트 처리
     * 주문을 실패(취소) 상태로 변경 요청
     */
    public void handlePaymentCompletionFailed(PaymentCompletionFailedPayload payload) {
        UUID orderId = payload.getOrderId();
        log.warn("❌ 결제 실패로 인한 주문 취소 처리 - orderId={}, reason={}", orderId, payload.getFailureReason());
        try {
            orderServiceClient.cancelOrder(orderId.toString());
        } catch (Exception e) {
            log.error("❌ 주문 취소 처리 실패 - orderId={}, error={}", orderId, e.getMessage());
        }
    }

    /**
     * 🔁 결제 취소 이벤트 처리
     * 주문을 취소 상태로 변경 요청
     */
    public void handlePaymentCancelled(PaymentCancelledPayload payload) {
        UUID orderId = payload.getOrderId();
        log.info("🔁 결제 취소에 따른 주문 취소 처리 - orderId={}", orderId);
        try {
            orderServiceClient.cancelOrder(orderId.toString());
        } catch (Exception e) {
            log.error("❌ 주문 취소 처리 실패 - orderId={}, error={}", orderId, e.getMessage());
        }
    }
}

