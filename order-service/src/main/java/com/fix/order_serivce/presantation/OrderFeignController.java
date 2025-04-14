package com.fix.order_serivce.presantation;

import com.fix.common_service.dto.CommonResponse;
import com.fix.order_serivce.application.OrderFeignService;
import com.fix.order_serivce.application.dtos.request.FeignOrderCreateRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/orders")
public class OrderFeignController {

    private final OrderFeignService orderFeignService;

    @PostMapping("/feign")
    public void createOrder(@RequestBody FeignOrderCreateRequest request) {
        orderFeignService.createOrderFromTicket(request);
    }

    @PatchMapping("/{orderId}/complete")
    public ResponseEntity<CommonResponse<Void>> completeOrder(@PathVariable UUID orderId) {
        orderFeignService.completeOrder(orderId);
        return ResponseEntity.ok(CommonResponse.success(null, "주문 상태가 COMPLETED로 변경되었습니다."));
    }

}