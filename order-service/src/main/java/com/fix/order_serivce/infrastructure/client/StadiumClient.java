package com.fix.order_serivce.infrastructure.client;

import com.fix.order_serivce.application.dtos.response.SeatPriceResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.UUID;


@FeignClient(name = "stadium-service")
public interface StadiumClient {

    @PostMapping("/seats/prices")
    List<SeatPriceResponse> getSeatPrices(@RequestBody List<UUID> seatIds);
}
