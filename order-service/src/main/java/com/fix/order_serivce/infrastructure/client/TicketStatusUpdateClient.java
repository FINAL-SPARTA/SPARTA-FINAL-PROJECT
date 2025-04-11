package com.fix.order_serivce.infrastructure.client;

import com.fix.order_serivce.application.dtos.request.FeignTicketSoldRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "ticket-status-update", url = "${feign.ticket-service.url}")
public interface TicketStatusUpdateClient {

    @PatchMapping("/sold")
    void updateTicketStatus(@RequestBody FeignTicketSoldRequest request);
}