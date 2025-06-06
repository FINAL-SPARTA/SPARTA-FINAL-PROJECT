package com.fix.common_service.kafka.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * ✅ 환불 성공 이벤트 Kafka Payload
 * - orderId 기반으로 주문 상태를 CANCELLED로 변경하기 위한 최소 정보만 포함
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PaymentCancelledPayload {

    /**
     * 주문 ID (Kafka 키 및 상태 변경 대상)
     */
    private UUID orderId;
}
