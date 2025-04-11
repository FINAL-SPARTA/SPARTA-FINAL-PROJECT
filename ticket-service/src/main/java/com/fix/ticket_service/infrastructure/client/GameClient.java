package com.fix.ticket_service.infrastructure.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.UUID;

@FeignClient(name = "game-service")
public interface GameClient {
    @PatchMapping("/api/v1/games/feign/{gameId}")
    void updateRemainingSeats(@PathVariable("gameId") UUID gameId,
        @RequestParam("quantity") int quantity);
}
