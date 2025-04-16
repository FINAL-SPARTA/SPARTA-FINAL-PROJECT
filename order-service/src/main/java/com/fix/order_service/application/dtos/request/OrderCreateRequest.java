package com.fix.order_service.application.dtos.request;

import lombok.Getter;

import java.util.UUID;

@Getter
public class OrderCreateRequest {
    private Long userId;
    private UUID gameId;
    private int peopleCount;
    private int totalPrice;
}
