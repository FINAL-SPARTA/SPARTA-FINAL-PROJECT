package com.fix.game_service.application.dtos.request;

import java.time.LocalDateTime;
import java.util.UUID;

import com.fix.game_service.domain.model.Team;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GameSearchRequest {

	private Team gameTeam1;
	private Team gameTeam2;
	private LocalDateTime gameDate;
	private Long stadiumId;

}
