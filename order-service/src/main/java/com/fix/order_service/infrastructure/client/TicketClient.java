package com.fix.order_service.infrastructure.client;

import java.util.UUID;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.fix.order_service.application.dtos.request.FeignTicketSoldRequest;

@FeignClient(name = "ticket-service")
public interface TicketClient {

    // 티켓 상태를 SOLD로 변경
    @PostMapping("/api/v1/tickets/sold")
    void updateTicketStatus(@RequestBody FeignTicketSoldRequest request);

    // 주문 취소 시 티켓 상태를 CANCELED로 변경
    @PostMapping("/api/v1/tickets/cancel/{orderId}")
    void cancelTicketStatus(@PathVariable("orderId") UUID orderId);
}