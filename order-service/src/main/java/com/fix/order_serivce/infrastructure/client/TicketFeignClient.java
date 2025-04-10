package com.fix.order_serivce.infrastructure.client;


import com.fix.order_serivce.application.dtos.request.FeignTicketSoldRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;

@FeignClient(name = "ticket-service", url = "${feign.ticket-service.url}")
public interface TicketFeignClient {

    @PatchMapping("/sold")
    void updateTicketStatus(@RequestBody FeignTicketSoldRequest request);

    @PatchMapping("/cancel/{orderId}")
    void cancelTicketStatus(@PathVariable("orderId") UUID orderId);
}