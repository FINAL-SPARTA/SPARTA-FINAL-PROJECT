package com.fix.payments_service.application.dtos.response;

import com.fix.payments_service.domain.TossPaymentMethod;
import com.fix.payments_service.domain.TossPaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.ZonedDateTime;

@Getter
@Builder
@AllArgsConstructor
public class TossPaymentStatusResponse {

    private String orderId;
    private TossPaymentMethod method;
    private TossPaymentStatus status;
    private int amount;
    private ZonedDateTime approvedAt;
}
