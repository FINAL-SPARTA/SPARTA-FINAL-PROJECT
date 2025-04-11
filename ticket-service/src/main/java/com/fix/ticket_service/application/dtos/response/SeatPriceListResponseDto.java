package com.fix.ticket_service.application.dtos.response;

import lombok.AllArgsConstructor;
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
@AllArgsConstructor
public class SeatPriceListResponseDto {
    private List<SeatPriceResponseDto> seatPricesList;

    public Map<UUID, Integer> toMap() {
        return seatPricesList.stream()
                .collect(Collectors.toMap(SeatPriceResponseDto::getSeatId, SeatPriceResponseDto::getPrice));
    }
}
