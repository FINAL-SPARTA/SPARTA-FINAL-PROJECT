package com.fix.order_serivce.presantation;

import com.fix.order_serivce.application.OrderFeignService;
import com.fix.order_serivce.application.OrderService;
import com.fix.order_serivce.application.dtos.request.FeignOrderCreateRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/orders/feign")
public class OrderFeignController {

    private final OrderFeignService orderFeignService;

    @PostMapping
    public void createOrder(@RequestBody FeignOrderCreateRequest request) {
        orderFeignService.createOrderFromTicket(request);
    }

    @PostMapping("/cancel/{orderId}")
    public void cancelOrder(@PathVariable UUID orderId) {
        orderFeignService.cancelOrderFromTicket(orderId);
    }
}