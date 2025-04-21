package com.fix.common_service.kafka.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

/**
 * 주문이 생성되었을 때 Kafka로 발행되는 이벤트의 Payload입니다.
 * - ticketIds: 예약된 티켓들의 ID 목록
 * - orderId: 생성된 주문의 ID
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrderCreatedPayload {
    private UUID orderId;
    private List<UUID> ticketIds;
    private int totalPrice;
}
