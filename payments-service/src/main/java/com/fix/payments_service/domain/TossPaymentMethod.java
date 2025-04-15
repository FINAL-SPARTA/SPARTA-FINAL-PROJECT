package com.fix.payments_service.domain;

public enum TossPaymentMethod {
    CARD("카드"),
    VIRTUAL_ACCOUNT("가상계좌"),
    ACCOUNT_TRANSFER("계좌이체"),
    MOBILE_PHONE("휴대폰"),
    TOSS_PAY("토스페이"),
    PAYCO("페이코"),
    SIMPLE_PAYMENT("간편결제"),
    GIFT_CARD("기프트카드"),
    CULTURE_GIFT_CARD("문화상품권");

    private final String korName;

    TossPaymentMethod(String korName) {
        this.korName = korName;
    }

    public String getKorName() {
        return korName;
    }

    public static TossPaymentMethod fromKorName(String name) {
        for (TossPaymentMethod method : values()) {
            if (method.getKorName().equals(name)) {
                return method;
            }
        }
        throw new IllegalArgumentException("지원하지 않는 결제 수단: " + name);
    }
}
