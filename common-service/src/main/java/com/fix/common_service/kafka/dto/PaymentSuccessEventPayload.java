package com.fix.common_service.kafka.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PaymentSuccessEventPayload {
    private UUID orderId;
    private String paymentKey;
    private long amount;
    private List<UUID> ticketIds;

    public PaymentSuccessEventPayload(UUID orderId, List<UUID> ticketIds, int amount) {
        this.orderId = orderId;
        this.ticketIds = ticketIds;
        this.amount = amount;
    }
}
