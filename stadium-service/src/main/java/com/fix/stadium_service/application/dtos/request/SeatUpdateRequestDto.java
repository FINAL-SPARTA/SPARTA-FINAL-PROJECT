package com.fix.stadium_service.application.dtos.request;

import com.fix.stadium_service.domain.model.SeatSection;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

import java.util.UUID;

@Getter
public class SeatUpdateRequestDto {

    @NotNull
    private UUID seatId;


    private Integer row;
    private Integer number;
    private SeatSection section;


}
