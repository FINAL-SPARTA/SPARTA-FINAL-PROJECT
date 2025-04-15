package com.fix.game_service.infrastructure.client;

import com.fix.common_service.dto.StadiumFeignResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "stadium-service")
public interface StadiumClient {
	@GetMapping("/api/v1/stadiums/{home-team}/games")
	ResponseEntity<StadiumFeignResponse> getStadiumInfo(@PathVariable(name = "home-team") String homeTeam);
}