package com.fix.stadium_service.application.dtos.request;

import com.fix.stadium_service.domain.model.Stadium;
import com.fix.stadium_service.domain.model.StadiumName;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;


@Getter
@NoArgsConstructor
@AllArgsConstructor
public class StadiumCreateRequest {
    private StadiumName stadiumName;
    private List<SeatRequestDto> seats;

}
