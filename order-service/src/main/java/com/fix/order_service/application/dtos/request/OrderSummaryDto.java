package com.fix.order_service.application.dtos.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;
import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderSummaryDto {
    private UUID orderId;
    private UUID gameId;
    private int peopleCount;
    private int totalPrice;
    private LocalDateTime createdAt;
}
