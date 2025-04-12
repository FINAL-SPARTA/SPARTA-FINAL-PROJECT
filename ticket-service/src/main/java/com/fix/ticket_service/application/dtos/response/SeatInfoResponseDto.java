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
    // 여기서 가격을 가져오면 티켓 예매 시 가격도 Request 로 넣을 수 있나??
//    private int price;
}

