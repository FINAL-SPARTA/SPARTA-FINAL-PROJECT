package com.fix.order_service.presantation;

import com.fix.common_service.dto.CommonResponse;
import com.fix.order_service.application.OrderFeignService;
import com.fix.order_service.application.dtos.request.FeignOrderCreateRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
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
    public ResponseEntity<CommonResponse<Void>> completeOrder(
            @PathVariable UUID orderId,
            @RequestBody List<UUID> ticketIds // 👈 전파 전략에 따라 request로 받는다
    ) {
        orderFeignService.completeOrder(orderId, ticketIds);
        return ResponseEntity.ok(CommonResponse.success(null, "주문 상태가 COMPLETED로 변경되었습니다."));
    }

}