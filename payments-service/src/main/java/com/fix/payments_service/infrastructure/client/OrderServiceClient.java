package com.fix.payments_service.infrastructure.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "order-service", url = "${feign.order-service.url}")
public interface OrderServiceClient {

    @PatchMapping("/api/v1/orders/{orderId}/complete")
    void completeOrder(@PathVariable("orderId") String orderId);

    @PatchMapping("/api/v1/orders/{orderId}/cancel")
    void cancelOrder(@PathVariable("orderId") String orderId);
}
