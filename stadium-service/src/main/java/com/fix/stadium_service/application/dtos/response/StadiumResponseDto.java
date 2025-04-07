package com.fix.stadium_service.application.dtos.response;


import com.fix.stadium_service.domain.model.Stadium;
import com.fix.stadium_service.domain.model.StadiumName;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@NoArgsConstructor
public class StadiumResponseDto {

    private UUID stadiumId;
    private StadiumName stadiumName;
    private Integer quantity;

    public StadiumResponseDto(Stadium stadium){
        this.stadiumId = stadium.getStadiumId();
        this.stadiumName = stadium.getStadiumName();
        this.quantity = stadium.getQuantity();
    }

}
