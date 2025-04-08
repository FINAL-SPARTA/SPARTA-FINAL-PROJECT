package com.fix.game_service.infrastructure.client.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StadiumResponseDto {

	private Long stadiumId;
	private String stadiumName;
	private Integer seatQuantity;

}
