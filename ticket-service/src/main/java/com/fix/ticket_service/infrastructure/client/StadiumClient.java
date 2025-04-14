package com.fix.ticket_service.infrastructure.client;

import com.fix.ticket_service.application.dtos.request.SeatPriceRequestDto;
import com.fix.ticket_service.application.dtos.response.SeatInfoListResponseDto;
import com.fix.ticket_service.application.dtos.response.SeatPriceListResponseDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@FeignClient(name = "stadium-service")
public interface StadiumClient {

    @PostMapping("/api/v1/stadiums/feign/get-prices")
    SeatPriceListResponseDto getPrices(@RequestBody SeatPriceRequestDto seatRequestDto);

    @GetMapping("/api/v1/stadiums/feign/{stadiumId}/get-seats-by-section")
    SeatInfoListResponseDto getSeatsBySection(@PathVariable("stadiumId") UUID stadiumId,
                                              @RequestParam("section") String section);

}
