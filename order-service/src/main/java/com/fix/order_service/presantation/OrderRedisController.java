package com.fix.order_service.presantation;

import com.fix.common_service.dto.CommonResponse;
import com.fix.order_service.application.OrderHistoryRedisService;
import com.fix.order_service.application.dtos.request.OrderSummaryDto;
import com.fix.order_service.application.aop.ValidateUser;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/orders/redis")
public class OrderRedisController {

    private final OrderHistoryRedisService orderHistoryRedisService;

    /**
     * Redis에서 유저별 최근 주문 내역 조회
     * @param userId 헤더로 전달되는 사용자 ID
     * @return 최근 주문 10건
     */
    @GetMapping("/history/recent")
    @ValidateUser(roles = {"ROLE_CUSTOMER"})
    public ResponseEntity<CommonResponse<List<OrderSummaryDto>>> getRecentOrders(
            @RequestHeader("X-User-Id") Long userId
    ) {
        List<OrderSummaryDto> recentOrders = orderHistoryRedisService.getRecentOrders(userId);
        return ResponseEntity.ok(CommonResponse.success(recentOrders, "최근 주문 내역 조회 성공"));
    }
}
//GET /api/v1/orders/redis/history/recent
//Header: X-User-Id: {유저번호}