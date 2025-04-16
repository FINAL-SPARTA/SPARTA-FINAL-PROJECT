package com.fix.order_service.domain;

public enum OrderStatus {
    CREATED,    // 예매 생성
    COMPLETED,  // 예매 완료
    CANCELLED   // 예매 취소시, 좌석 재고 반환
}
