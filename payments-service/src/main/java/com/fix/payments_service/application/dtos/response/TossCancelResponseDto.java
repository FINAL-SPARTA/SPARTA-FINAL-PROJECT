package com.fix.payments_service.application.dtos.response;

import com.fix.payments_service.domain.TossPaymentStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;

@Getter
@NoArgsConstructor
public class TossCancelResponseDto {

    private String paymentKey;
    private String orderId;
    private TossPaymentStatus status;
    private ZonedDateTime canceledAt;
    private String cancelReason;
    private Integer cancelAmount;
}
