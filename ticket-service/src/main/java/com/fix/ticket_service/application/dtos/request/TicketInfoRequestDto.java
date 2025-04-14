package com.fix.ticket_service.application.dtos.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class TicketInfoRequestDto {
    private UUID seatId;
    private int price;
}
