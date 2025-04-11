package com.fix.ticket_service.application.dtos.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Getter
@Setter
@NoArgsConstructor
public class SeatPriceListResponseDto {
    private List<SeatPriceResponseDto> seatPriceList;

    public Map<UUID, Integer> toMap() {
        return seatPriceList.stream()
                .collect(Collectors.toMap(SeatPriceResponseDto::getSeatId, SeatPriceResponseDto::getPrice));
    }
}
