package com.fix.ticket_service.application.dtos.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TicketReservationResponseDto {
    private UUID reservationRequestId; // 예약 요청 ID
}
