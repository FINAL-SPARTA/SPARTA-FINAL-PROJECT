package com.fix.payments_service.application;

import com.fix.common_service.kafka.dto.*;
import com.fix.payments_service.domain.TossPayment;
import com.fix.payments_service.domain.TossPaymentFailure;
import com.fix.payments_service.domain.TossPaymentMethod;
import com.fix.payments_service.domain.TossPaymentStatus;
import com.fix.payments_service.domain.repository.TossPaymentFailureRepository;
import com.fix.payments_service.domain.repository.TossPaymentRepository;
import com.fix.payments_service.infrastructure.kafka.PaymentProducer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentEventService {

    private final PaymentProducer paymentProducer;
    private final TossPaymentRepository tossPaymentRepository;
    private final TossPaymentFailureRepository tossPaymentFailureRepository;
    /**
     * ✅ 결제 완료 → Kafka로 주문 상태 COMPLETED 이벤트 발행 (단순 이벤트 발행 헬퍼)
     */
    public void sendPaymentCompleted(UUID orderId, List<UUID> ticketIds, int totalPrice) {
        PaymentCompletedPayload payload = new PaymentCompletedPayload(orderId, "mock-key", totalPrice, ticketIds);
        paymentProducer.sendPaymentCompletedEvent(payload);
        log.info("✅ 주문 상태 COMPLETED 이벤트 발행 완료 - orderId: {}", orderId);
    }

    /**
     * 결제 실패 이벤트 발행
     */
    public void sendPaymentCompletionFailed(UUID orderId, String failureReason) {
        PaymentCompletionFailedPayload payload = new PaymentCompletionFailedPayload(orderId, failureReason);
        paymentProducer.sendPaymentCompletionFailedEvent(payload);
        log.info("❌ 결제 실패 이벤트 발행 완료 - orderId: {}, reason: {}", orderId, failureReason);
    }

    /**
     * ✅ 환불 완료 → Kafka로 주문 상태 CANCELLED 이벤트 발행
     */
    public void sendPaymentCancelled(UUID orderId) {
        PaymentCancelledPayload payload = new PaymentCancelledPayload(orderId);
        paymentProducer.sendPaymentCancelledEvent(payload);
        log.info("✅ 주문 상태 CANCELLED 이벤트 발행 완료 - orderId: {}", orderId);
    }


//    결제 이벤트 수신 → 처리 → 결과 발행
    public void processPaymentRequest(OrderCreatedPayload payload) {
        UUID orderId = payload.getOrderId();
        List<UUID> ticketIds = payload.getTicketIds();

        log.info("💳 결제 요청 처리 시작: orderId={}, ticketCount={}", orderId, ticketIds.size());


        try {
            // ✅ (1) 결제 mock 데이터 생성
            TossPayment tossPayment = TossPayment.builder()
                    .orderId(orderId)
                    .paymentKey(UUID.randomUUID().toString())
                    .amount(payload.getTotalPrice())
                    .method(TossPaymentMethod.CARD)
                    .status(TossPaymentStatus.DONE)
                    .build();

        // ✅ (2) DB 저장
            tossPaymentRepository.save(tossPayment);

        // ✅ (3) Kafka 이벤트 발행 (정상 흐름)
            PaymentCompletedPayload completedPayload = new PaymentCompletedPayload(
                    tossPayment.getOrderId(),
                    tossPayment.getPaymentKey(),
                    tossPayment.getAmount(),
                    ticketIds
            );
            paymentProducer.sendPaymentCompletedEvent(completedPayload);
            log.info("✅ PaymentCompleted 이벤트 발행 완료: orderId={}", orderId);

        } catch (Exception e) {
            log.error("❌ 결제 처리 실패: {}", e.getMessage());

        // ✅ (4) 실패 정보 저장
            TossPaymentFailure failure = TossPaymentFailure.builder()
                .orderId(orderId.toString())
                .paymentKey(null)             // 값이 없는 경우 null 또는 생략
                .errorCode(null)              // 필요 시 지정
                .errorMessage(e.getMessage()) // ✅ 적절한 필드명으로 교체
                .build();
            tossPaymentFailureRepository.save(failure);

        // ✅ (5) 실패 이벤트 발행
            PaymentCompletionFailedPayload failedPayload = new PaymentCompletionFailedPayload(orderId, e.getMessage());
            paymentProducer.sendPaymentCompletionFailedEvent(failedPayload);

            log.info("📤 PaymentCompletionFailed 이벤트 발행 완료: orderId={}", orderId);
    }
}
}
