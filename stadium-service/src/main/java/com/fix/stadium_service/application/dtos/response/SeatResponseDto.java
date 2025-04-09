package com.fix.stadium_service.application.dtos.response;

import com.fix.stadium_service.domain.model.Seat;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@NoArgsConstructor
public class SeatResponseDto {
    private UUID seatId;
    private Integer row;
    private Integer number;
    private String section;
    public SeatResponseDto(Seat seat) {
        this.seatId = seat.getSeatId();
        this.row = seat.getRow();
        this.number = seat.getNumber();
        this.section = seat.getSection().name();

    }
}