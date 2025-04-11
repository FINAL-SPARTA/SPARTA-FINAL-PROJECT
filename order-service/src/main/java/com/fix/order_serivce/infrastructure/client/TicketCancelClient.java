package com.fix.order_serivce.infrastructure.client;


import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

@FeignClient(name = "ticket-cancel", url = "${feign.ticket-service.url}")
public interface TicketCancelClient {

    @PatchMapping("/cancel/{orderId}")
    void cancelTicketStatus(@PathVariable("orderId") UUID orderId);
}