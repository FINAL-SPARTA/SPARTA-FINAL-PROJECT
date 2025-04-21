package com.fix.common_service.kafka.dto;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * ✅ 결제 성공 이벤트 Kafka Payload
 * - orderId 기반으로 주문 상태를 COMPLETED로 변경하기 위한 최소 정보만 포함
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PaymentCompletedPayload {

    /**
     * 주문 ID (Kafka 키 및 상태 변경 대상)
     */
    private UUID orderId;
    private String paymentKey;  // 가짜 결제 키
    private long amount;        // 결제 금액

    public PaymentCompletedPayload(UUID orderId) {
        this.orderId = orderId;
        this.paymentKey = "mock-key"; // 또는 null
        this.amount = 0L;
    }
}
