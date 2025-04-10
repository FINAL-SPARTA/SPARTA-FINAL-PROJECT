package com.fix.ticket_service.domain.model;

public enum TicketStatus {
    AVAILABLE,  // 아직 예약 및 결제가 안 되었고, 예약 가능한 상태 (사실 이 상태는 딱히 필요 없긴 함)
    RESERVED,   // 예약(결제 대기)
    SOLD       // 판매(결제) 완료
}
