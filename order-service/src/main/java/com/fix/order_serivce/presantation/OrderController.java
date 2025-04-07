package com.fix.order_serivce.presantation;

import com.fix.common_service.dto.CommonResponse;
import com.fix.order_serivce.application.OrderService;
import com.fix.order_serivce.application.dtos.request.OrderCreateRequest;
import com.fix.order_serivce.application.dtos.request.OrderUpdateRequest;
import com.fix.order_serivce.application.dtos.response.OrderDetailResponse;
import com.fix.order_serivce.application.dtos.response.OrderResponse;
import com.fix.order_serivce.domain.Ticket;
import com.fix.order_serivce.domain.repository.TicketRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final TicketRepository ticketRepository;

    @PostMapping
    public ResponseEntity<CommonResponse<OrderResponse>> createOrder(@RequestBody OrderCreateRequest request) {
        UUID orderId = orderService.createOrder(request);

        // 생성된 티켓 ID 목록을 조회
        List<UUID> ticketIds = ticketRepository.findAllByOrderId(orderId)
                .stream()
                .map(Ticket::getTicketId)
                .toList();

        OrderResponse response = OrderResponse.builder()
                .orderId(orderId)
                .userId(request.getUserId())
                .gameId(request.getGameId())
                .peopleCount(request.getPeopleCount())
                .totalCount(ticketIds.size())
                .ticketIds(ticketIds)
                .build();

        return ResponseEntity.ok(CommonResponse.created(response, "주문이 생성되었습니다."));
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<CommonResponse<OrderDetailResponse>> getOrder(@PathVariable UUID orderId) {
        OrderDetailResponse response = orderService.getOrder(orderId);
        return ResponseEntity.ok(CommonResponse.success(response, "주문 조회 성공"));
    }

    @PutMapping("/{orderId}")
    public ResponseEntity<CommonResponse<Void>> updateOrder(
            @PathVariable UUID orderId,
            @RequestBody OrderUpdateRequest request
    ) {
        orderService.updateOrder(orderId, request);
        return ResponseEntity.ok(CommonResponse.success(null, "주문 정보가 수정되었습니다."));
    }

    @DeleteMapping("/{orderId}")
    public ResponseEntity<CommonResponse<Void>> deleteOrder(
            @PathVariable UUID orderId,
            @RequestHeader("X-User-Id") Long userId // softDelete에 필요
    ) {
        orderService.deleteOrder(orderId, userId);
        return ResponseEntity.ok(CommonResponse.success(null, "주문이 취소되었습니다."));
    }
}
