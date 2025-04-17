package com.fix.payments_service.infrastructure.kafka;

import com.fix.common_service.dto.PaymentCancelledPayload;
import com.fix.common_service.dto.PaymentCompletedPayload;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    private static final String PAYMENT_COMPLETED_TOPIC = "payment-completed-topic";
    private static final String PAYMENT_CANCELLED_TOPIC = "payment-cancelled-topic";

    /**
     * ✅ 결제 성공 시 Kafka 이벤트 발행
     */
    public void sendPaymentCompletedEvent(PaymentCompletedPayload payload) {
        log.info("📤 Kafka 전송: 결제 완료 - {}", payload);
        kafkaTemplate.send(PAYMENT_COMPLETED_TOPIC, payload.getOrderId().toString(), payload);
    }

    /**
     * ✅ 환불 성공 시 Kafka 이벤트 발행
     */
    public void sendPaymentCancelledEvent(PaymentCancelledPayload payload) {
        log.info("📤 Kafka 전송: 환불 완료 - {}", payload);
        kafkaTemplate.send(PAYMENT_CANCELLED_TOPIC, payload.getOrderId().toString(), payload);
    }
}
