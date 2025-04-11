package com.fix.stadium_service.application.dtos.request;


import com.fix.stadium_service.domain.model.SeatSection;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class SeatRequestDto {
    private Integer row;
    private Integer number;
    private SeatSection section;


}


