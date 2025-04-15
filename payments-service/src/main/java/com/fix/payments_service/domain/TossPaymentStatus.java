package com.fix.payments_service.domain;

public enum TossPaymentStatus {
    READY("결제대기"),
    IN_PROGRESS("결제중"),
    WAITING_FOR_DEPOSIT("입금대기"),
    DONE("결제완료"),
    CANCELED("결제취소"),
    PARTIAL_CANCELED("부분취소"),
    ABORTED("결제실패"),
    EXPIRED("만료됨");

    private final String korName;

    TossPaymentStatus(String korName) {
        this.korName = korName;
    }

    public String getKorName() {
        return korName;
    }

    public static TossPaymentStatus fromKorName(String name) {
        for (TossPaymentStatus status : values()) {
            if (status.getKorName().equals(name)) {
                return status;
            }
        }
        throw new IllegalArgumentException("지원하지 않는 결제 상태: " + name);
    }

    public static TossPaymentStatus fromNameOrKorName(String name) {
        for (TossPaymentStatus status : values()) {
            if (status.name().equalsIgnoreCase(name) || status.getKorName().equals(name)) {
                return status;
            }
        }
        throw new IllegalArgumentException("지원하지 않는 결제 상태: " + name);
    }
}

