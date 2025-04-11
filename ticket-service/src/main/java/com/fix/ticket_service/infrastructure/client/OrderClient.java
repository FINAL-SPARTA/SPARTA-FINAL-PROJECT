package com.fix.ticket_service.infrastructure.client;

import com.fix.ticket_service.application.dtos.request.OrderCreateRequestDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "order-service")
public interface OrderClient {

    @PostMapping("/api/v1/orders/feign")
    void createOrder(@RequestBody OrderCreateRequestDto requestDto);
}
