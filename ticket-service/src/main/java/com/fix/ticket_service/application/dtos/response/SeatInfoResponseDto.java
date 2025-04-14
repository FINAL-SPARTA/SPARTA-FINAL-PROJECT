package com.fix.ticket_service.application.dtos.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class SeatInfoResponseDto {
    private UUID seatId;
    private String section;
    private String seatRow;
    private String seatNumber;
    private int price;
}

