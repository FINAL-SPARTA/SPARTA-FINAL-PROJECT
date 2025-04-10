package com.fix.stadium_service.application.dtos.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
public class SeatPriceListResponseDto {
    private List<SeatPriceResponseDto> seatPricesList;

    public SeatPriceListResponseDto(List<SeatPriceResponseDto> seatPricesList) {
        this.seatPricesList = seatPricesList;
    }
}
