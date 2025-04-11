package com.fix.ticket_service.infrastructure.client;

import java.util.UUID;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "game-service")
public interface GameClient {
    @PostMapping("/api/v1/games/feign/{gameId}")
    void updateRemainingSeats(@PathVariable("gameId") UUID gameId,
        @RequestParam("quantity") int quantity);
}
