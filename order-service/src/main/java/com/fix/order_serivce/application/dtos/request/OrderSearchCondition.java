package com.fix.order_serivce.application.dtos.request;

import com.fix.order_serivce.domain.OrderStatus;
import lombok.Getter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.util.UUID;

@Getter
public class OrderSearchCondition {
    private Long userId;
    private UUID gameId;
    private OrderStatus orderStatus;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate fromDate;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate toDate;
}