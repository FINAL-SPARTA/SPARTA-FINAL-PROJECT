package com.fix.stadium_service.application.dtos.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class SeatSectionListResponseDto {
    private List<String> sections;

}
