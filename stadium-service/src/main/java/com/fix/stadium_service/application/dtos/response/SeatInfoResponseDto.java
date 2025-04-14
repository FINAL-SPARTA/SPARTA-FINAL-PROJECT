package com.fix.stadium_service.application.dtos.response;


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
    private Integer seatRow;
    private Integer seatNumber;
    private int price;

}
