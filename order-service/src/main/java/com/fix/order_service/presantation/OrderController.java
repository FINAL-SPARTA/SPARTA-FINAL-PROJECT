package com.fix.order_service.presantation;

import com.fix.common_service.dto.CommonResponse;
import com.fix.order_service.application.OrderService;
import com.fix.order_service.application.aop.ValidateUser;
import com.fix.order_service.application.dtos.request.OrderSearchCondition;
import com.fix.order_service.application.dtos.request.OrderUpdateRequest;
import com.fix.order_service.application.dtos.response.OrderDetailResponse;
import com.fix.order_service.application.dtos.response.OrderResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    // 단건 조회
    @ValidateUser(roles = {"ROLE_CUSTOMER", "ROLE_MANAGER", "ROLE_MASTER"})
    @GetMapping("/{orderId}")
    public ResponseEntity<CommonResponse<OrderDetailResponse>> getOrder(@PathVariable UUID orderId) {
        OrderDetailResponse response = orderService.getOrder(orderId);
        return ResponseEntity.ok(CommonResponse.success(response, "주문 조회 성공"));
    }

    // 전체 조회 (페이징)
    @ValidateUser(roles = {"ROLE_MANAGER", "ROLE_MASTER"})
    @GetMapping
    public ResponseEntity<CommonResponse<Page<OrderResponse>>> getOrders(Pageable pageable) {
        Page<OrderResponse> orders = orderService.getOrders(pageable);
        return ResponseEntity.ok(CommonResponse.success(orders, "주문 전체 조회 성공"));
    }

    // 검색
    @ValidateUser(roles = {"ROLE_MANAGER", "ROLE_MASTER"})
    @GetMapping("/search")
    public ResponseEntity<CommonResponse<Page<OrderResponse>>> searchOrders(
            @ModelAttribute OrderSearchCondition condition,
            Pageable pageable
    ) {
        Page<OrderResponse> result = orderService.searchOrders(condition, pageable);
        return ResponseEntity.ok(CommonResponse.success(result, "주문 검색 결과"));
    }

    // 주문 수정
    @ValidateUser(roles = {"ROLE_CUSTOM"})
    @PutMapping("/{orderId}")
    public ResponseEntity<CommonResponse<Void>> updateOrder(
            @PathVariable UUID orderId,
            @RequestBody OrderUpdateRequest request
    ) {
        orderService.updateOrder(orderId, request);
        return ResponseEntity.ok(CommonResponse.success(null, "주문 정보가 수정되었습니다."));
    }

    // 주문 취소
    @PatchMapping("/cancel/{orderId}")
    public void cancelOrder(@PathVariable UUID orderId) {
        orderService.cancelOrderFromTicket(orderId);
    }

    // 주문 취소 (soft delete)
    @ValidateUser(roles = {"ROLE_MANAGER", "ROLE_MASTER", "ROLE_CUSTOM"})
    @DeleteMapping("/{orderId}")
    public ResponseEntity<CommonResponse<Void>> deleteOrder(
            @PathVariable UUID orderId,
            @RequestHeader("X-User-Id") Long userId // softDelete에 필요
    ) {
        orderService.deleteOrder(orderId, userId);
        return ResponseEntity.ok(CommonResponse.success(null, "주문이 취소되었습니다."));
    }
}
