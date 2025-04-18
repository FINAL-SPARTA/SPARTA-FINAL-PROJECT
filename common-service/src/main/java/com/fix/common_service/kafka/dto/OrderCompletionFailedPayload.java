package com.fix.common_service.kafka.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrderCompletionFailedPayload {
    private List<UUID> ticketIds;
    private Long userId;
    private UUID gameId;
    private UUID orderId;
    private String failureReason; // 실패 사유
}
