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
public class TicketReservationRequestPayload {
    private UUID reservationRequestId; // 추적용 ID
    private Long userId;
    private String queueToken; // 큐 토큰 (워커에서 최종 검증을 위해 필요)
    private UUID gameId;
    TicketInfoPayload ticketToProcess; // 예약할 티켓
    List<TicketInfoPayload> originalTicketList; // 예약할 티켓의 원본 정보 (예약 요청 시점의 정보)
    int totalTicketsInRequest; // 원본 요청의 총 티켓(좌석) 수 (결과 집계용)
}
