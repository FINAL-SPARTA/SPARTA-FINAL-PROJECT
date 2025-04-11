package com.fix.stadium_service.application.dtos.request;

import com.fix.stadium_service.domain.model.StadiumName;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class StadiumUpdateRequest {
    private StadiumName stadiumName;
    private Integer quantity;

    @Valid
    private List<SeatUpdateRequestDto> seats;


}
