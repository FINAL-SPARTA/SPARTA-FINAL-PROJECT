package com.fix.stadium_service.application.dtos.response;

import com.fix.stadium_service.domain.model.Stadium;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class StadiumFeignResponse {

    private Long stadiumId;
    private String stadiumName;
    private Integer seatQuantity;

    public static StadiumFeignResponse from(Stadium stadium){
        return new StadiumFeignResponse(
                stadium.getStadiumId(),
                stadium.getStadiumName().name(),
                stadium.getStadiumName().getSeatCapacity()
        );
    }

}
