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
public class TicketReservationSucceededPayload {
    private UUID reservationRequestId; // 추적용 ID
    private Long userId;
    private UUID gameId;
    List<ReservedTicketInfo> ticketDetails; // 예약에 성공한 티켓 정보

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReservedTicketInfo {
        private UUID ticketId;
        private UUID seatId;
        private int price;
    }
}
