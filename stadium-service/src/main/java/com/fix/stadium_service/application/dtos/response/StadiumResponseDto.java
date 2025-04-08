package com.fix.stadium_service.application.dtos.response;


import com.fix.stadium_service.domain.model.Stadium;
import com.fix.stadium_service.domain.model.StadiumName;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Getter
@NoArgsConstructor
public class StadiumResponseDto {

    private UUID stadiumId;
    private StadiumName stadiumName;
    private Integer quantity;
    private List<SeatResponseDto> seats;

    public StadiumResponseDto(Stadium stadium) {
        this.stadiumId = stadium.getStadiumId();
        this.stadiumName = stadium.getStadiumName();
        this.quantity = stadium.getQuantity();
        this.seats = stadium.getSeats().stream()
                .map(SeatResponseDto::new)
                .toList();

    }

}
