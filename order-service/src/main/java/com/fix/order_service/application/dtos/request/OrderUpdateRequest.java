package com.fix.order_service.application.dtos.request;

import com.fix.order_service.domain.OrderStatus;
import lombok.Getter;

@Getter
public class OrderUpdateRequest {
    private int peopleCount;
    private OrderStatus orderStatus;
}
