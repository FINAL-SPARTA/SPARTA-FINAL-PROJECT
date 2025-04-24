package com.fix.payments_service.application.dtos.response;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;

@Getter
@NoArgsConstructor
public class TossConfirmResponseDto {
    private String orderId;
    private String paymentKey;
    private String status;
    private int totalAmount;
    private ZonedDateTime approvedAt;
    private String method;

    private CardInfo card;
    private ReceiptInfo receipt;

    @Getter
    @NoArgsConstructor
    public static class CardInfo {
        private String number;
        private String company;
    }

    @Getter
    @NoArgsConstructor
    public static class ReceiptInfo {
        private String url;
    }
}

