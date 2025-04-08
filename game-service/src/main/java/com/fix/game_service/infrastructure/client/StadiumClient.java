package com.fix.game_service.infrastructure.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.fix.game_service.infrastructure.client.dto.StadiumResponseDto;

@FeignClient(name = "stadium-service")
public interface StadiumClient {
	@GetMapping("/api/v1/stadiums/{home-team}/games")
	public StadiumResponseDto getStadiumInfo(@PathVariable(name = "home-team") String homeTeam);
}