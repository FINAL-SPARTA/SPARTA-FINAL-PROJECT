package com.fix.ticket_service.infrastructure.client;

import com.fix.ticket_service.application.dtos.response.SeatInfoListResponseDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.UUID;

@FeignClient(name = "stadium-service")
public interface StadiumClient {

    @GetMapping("/api/v1/stadiums/feign/{stadiumId}/get-seats-by-section")
    SeatInfoListResponseDto getSeatsBySection(@PathVariable("stadiumId") Long stadiumId,
                                              @RequestParam("section") String section);

}
