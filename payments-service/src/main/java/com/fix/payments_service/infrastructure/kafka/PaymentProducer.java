package com.fix.payments_service.infrastructure.kafka;

import com.fix.common_service.kafka.dto.*;
import com.fix.common_service.kafka.producer.KafkaProducerHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentProducer {

    private final KafkaProducerHelper kafkaProducerHelper;

    @Value("${kafka-topics.payment.completed}")
    private String paymentCompletedTopic;

    @Value("${kafka-topics.payment.completion-failed}")
    private String paymentCompletionFailedTopic;

    @Value("${kafka-topics.payment.cancelled}")
    private String paymentCancelledTopic;

    private <T> void send(String topic, String key, String type, T payload) {
        kafkaProducerHelper.send(topic, key, new EventKafkaMessage<>(type, payload));
    }
//  결제 완료 이벤트를 Kafka로 발행
    public void sendPaymentCompletedEvent(PaymentCompletedPayload payload) {
        send(paymentCompletedTopic, payload.getOrderId().toString(), "PAYMENT_COMPLETED", payload);
    }
//    결제 실패 이벤트를 Kafka로 발행
    public void sendPaymentCompletionFailedEvent(PaymentCompletionFailedPayload payload) {
        send(paymentCompletionFailedTopic, payload.getOrderId().toString(), "PAYMENT_COMPLETION_FAILED", payload);
    }
//    결제 취소 이벤트를 Kafka로 발행
    public void sendPaymentCancelledEvent(PaymentCancelledPayload payload) {
        send(paymentCancelledTopic, payload.getOrderId().toString(), "PAYMENT_CANCELLED", payload);
    }
}
//OrderProducer는 초기 방식 또는 명시적 타입 전달을 위한 구조
//PaymentProducer는 정적 팩토리 메서드 기반의 최신 스타일 -> Order처럼 수정

//항목	OrderProducer - 공통 로직 추상화 ,메시지 포맷 통일	✅ EventKafkaMessage, 메시지 타입 명시 - "TYPE" 전달, 구조 일관성