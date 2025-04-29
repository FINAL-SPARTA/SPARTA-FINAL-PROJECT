package com.fix.common_service.kafka.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TicketReservationFailedPayload {
    private UUID reservationRequestId; // 추적용 ID
    private Long userId;
    private UUID gameId;
    List<FailedSeatInfo> failedTicketDetails; // 예약에 실패한 좌석 정보

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FailedSeatInfo {
        private UUID seatId;
        private int price;
        private String failureReason; // 실패 사유
    }
}
