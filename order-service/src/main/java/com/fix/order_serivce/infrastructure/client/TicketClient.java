package com.fix.order_serivce.infrastructure.client;


import com.fix.order_serivce.application.dtos.request.FeignTicketSoldRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.UUID;

@FeignClient(name = "ticket-service")
public interface TicketClient {

    // 티켓 상태를 SOLD로 변경
    @PatchMapping("/sold")
    void updateTicketStatus(@RequestBody FeignTicketSoldRequest request);

    // 주문 취소 시 티켓 상태를 CANCELLED로 변경
    @PatchMapping("/cancel/{orderId}")
    void cancelTicketStatus(@PathVariable("orderId") UUID orderId);
}