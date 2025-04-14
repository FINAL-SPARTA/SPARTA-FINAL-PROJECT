package com.fix.ticket_service.application.dtos.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class TicketReserveRequestDto {
    private UUID gameId;
    private UUID stadiumId;
    private List<TicketInfoRequestDto> seatInfoList;
}
