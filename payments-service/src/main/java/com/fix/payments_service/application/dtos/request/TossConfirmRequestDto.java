package com.fix.payments_service.application.dtos.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class TossConfirmRequestDto {
    private String orderId;
    private int amount;
}
