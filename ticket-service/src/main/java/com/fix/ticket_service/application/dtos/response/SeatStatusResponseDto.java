package com.fix.ticket_service.application.dtos.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.io.Serializable;
import java.util.UUID;

@Getter
@AllArgsConstructor
public class SeatStatusResponseDto implements Serializable {
    private UUID seatId;
    private String section;
    private Integer seatRow;
    private Integer seatNumber;
    private int price;
    private Boolean availabilityStatus;
}

