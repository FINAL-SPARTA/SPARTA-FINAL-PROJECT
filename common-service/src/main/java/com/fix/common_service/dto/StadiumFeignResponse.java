package com.fix.common_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StadiumFeignResponse {

    private Long stadiumId;
    private String stadiumName;
    private Integer seatQuantity;

}