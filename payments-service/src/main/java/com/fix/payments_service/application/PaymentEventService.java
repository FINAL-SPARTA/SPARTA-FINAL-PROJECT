package com.fix.payments_service.application;

import com.fix.common_service.dto.PaymentCancelledPayload;
import com.fix.common_service.dto.PaymentCompletedPayload;
import com.fix.payments_service.infrastructure.kafka.PaymentProducer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;
@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentEventService {

    private final PaymentProducer paymentProducer;

    /**
     * ✅ 결제 완료 → Kafka로 주문 상태 COMPLETED 이벤트 발행
     */
    public void sendPaymentCompleted(UUID orderId) {
        PaymentCompletedPayload payload = new PaymentCompletedPayload(orderId);
        paymentProducer.sendPaymentCompletedEvent(payload);
        log.info("✅ 주문 상태 COMPLETED 이벤트 발행 완료 - orderId: {}", orderId);
    }

    /**
     * ✅ 환불 완료 → Kafka로 주문 상태 CANCELLED 이벤트 발행
     */
    public void sendPaymentCancelled(UUID orderId) {
        PaymentCancelledPayload payload = new PaymentCancelledPayload(orderId);
        paymentProducer.sendPaymentCancelledEvent(payload);
        log.info("✅ 주문 상태 CANCELLED 이벤트 발행 완료 - orderId: {}", orderId);
    }
}
