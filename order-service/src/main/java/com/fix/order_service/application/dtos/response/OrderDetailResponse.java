package com.fix.order_service.application.dtos.response;


import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
public class OrderDetailResponse {
    private UUID orderId;
    private Long userId;
    private UUID gameId;
    private int peopleCount;
    private int totalPrice;
}
