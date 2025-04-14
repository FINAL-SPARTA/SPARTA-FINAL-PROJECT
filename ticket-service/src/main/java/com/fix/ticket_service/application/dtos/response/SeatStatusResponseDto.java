package com.fix.ticket_service.application.dtos.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@Getter
@AllArgsConstructor
public class SeatStatusResponseDto {
    private UUID seatId;
    private String section;
    private String seatRow;
    private String seatNumber;
    private int price;
    private Boolean availabilityStatus;
}

