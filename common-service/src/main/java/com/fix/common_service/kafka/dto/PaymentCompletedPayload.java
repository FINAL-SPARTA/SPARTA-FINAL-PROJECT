package com.fix.common_service.kafka.dto;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

/**
 * ✅ 결제 성공 이벤트 Kafka Payload
 * - orderId 기반으로 주문 상태를 COMPLETED로 변경하기 위한 최소 정보만 포함
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PaymentCompletedPayload {
    private UUID orderId;
    private String paymentKey;
    private long amount;
    private List<UUID> ticketIds;

    // 최소 정보만 포함한 생성자
    public PaymentCompletedPayload(UUID orderId) {
        this.orderId = orderId;
        this.paymentKey = "mock-key"; // 또는 null
        this.amount = 0L;
        this.ticketIds = null;
    }
}
