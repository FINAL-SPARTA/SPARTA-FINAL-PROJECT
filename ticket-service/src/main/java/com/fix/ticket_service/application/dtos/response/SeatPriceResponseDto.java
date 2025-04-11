package com.fix.ticket_service.application.dtos.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
public class SeatPriceResponseDto {
    private UUID seatId;
    private int price;
}
