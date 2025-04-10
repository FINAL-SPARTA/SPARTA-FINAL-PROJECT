package com.fix.ticket_service.application.dtos.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Getter
@NoArgsConstructor
public class SeatPriceRequestDto {
    private List<UUID> seatIds;

    public SeatPriceRequestDto(List<UUID> seatIds) {
        this.seatIds = seatIds;
    }
}
