package com.fix.common_service.kafka.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PaymentCompletionFailedPayload {
    private UUID orderId;
    private String failureReason;
}
