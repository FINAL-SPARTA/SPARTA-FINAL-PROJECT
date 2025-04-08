package com.fix.order_serivce.application.dtos.request;

import com.fix.order_serivce.domain.OrderStatus;
import lombok.Getter;

@Getter
public class OrderUpdateRequest {
    private int peopleCount;
    private OrderStatus orderStatus;
}
