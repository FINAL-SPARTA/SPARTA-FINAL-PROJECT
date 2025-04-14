package com.fix.order_serivce.application;

import com.fix.order_serivce.application.dtos.request.OrderSearchCondition;
import com.fix.order_serivce.application.dtos.request.OrderUpdateRequest;
import com.fix.order_serivce.application.dtos.response.OrderDetailResponse;
import com.fix.order_serivce.application.dtos.response.OrderResponse;
import com.fix.order_serivce.domain.Order;
import com.fix.order_serivce.domain.repository.OrderQueryRepository;
import com.fix.order_serivce.domain.repository.OrderRepository;
import com.fix.order_serivce.application.exception.OrderException;
import com.fix.order_serivce.infrastructure.client.TicketClient;

import static com.fix.order_serivce.application.exception.OrderException.OrderErrorType.*;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderQueryRepository orderQueryRepository;
    private final TicketClient ticketClient;
    private final RedisTemplate<String, Object> redisTemplate;

//    단건 조회
    @Transactional(readOnly = true)
    public OrderDetailResponse getOrder(UUID orderId) {
        String key = "order:detail:" + orderId;

//        [1] Redis 캐시 조회
        OrderDetailResponse cached = (OrderDetailResponse) redisTemplate.opsForValue().get(key);
        if (cached != null) {
            return cached;
        }
//        [2] DB 조회
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderException(ORDER_NOT_FOUND));

        OrderDetailResponse response = OrderDetailResponse.builder()
                .orderId(order.getOrderId())
                .userId(order.getUserId())
                .gameId(order.getGameId())
                .peopleCount(order.getPeopleCount())
                .totalPrice(order.getTotalPrice())
                .build();

        // [3] 캐시에 저장 (TTL: 5분)
        redisTemplate.opsForValue().set(key, response, Duration.ofMinutes(5));

        return response;
    }

//    전체 조회(페이징)
    @Transactional(readOnly = true)
    public Page<OrderResponse> getOrders(Pageable pageable) {
        return orderRepository.findAll(pageable)
                .map(order -> OrderResponse.builder()
                        .orderId(order.getOrderId())
                        .userId(order.getUserId())
                        .gameId(order.getGameId())
                        .peopleCount(order.getPeopleCount())
                        .totalPrice(order.getTotalPrice())
                        .ticketIds(null) // 필요시 간단 목록용 필드로
                        .build());
    }

//    검색(Query DSL)
    @Transactional(readOnly = true)
    public Page<OrderResponse> searchOrders(OrderSearchCondition condition, Pageable pageable) {
        return orderQueryRepository.search(condition, pageable);
    }

//    주문 수정
    @Transactional
    public void updateOrder(UUID orderId, OrderUpdateRequest request) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderException(ORDER_NOT_FOUND));
        order.update(request.getPeopleCount(), request.getOrderStatus());

//        캐시 무효화
        redisTemplate.delete("order:detail:" + orderId);
    }

    // 주문 취소
    @Transactional
    public void cancelOrderFromTicket(UUID orderId) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new OrderException(ORDER_NOT_FOUND));

        // 주문 상태 변경 (soft delete 아님)
        order.cancel();

        // 티켓 상태도 CANCELLED로 변경 요청
        ticketClient.cancelTicketStatus(orderId);
    }

//    주문 삭제 (soft delete)
    @Transactional
    public void deleteOrder(UUID orderId, Long userId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderException(ORDER_NOT_FOUND));
        order.softDelete(userId);

        //    캐시 무효화
        redisTemplate.delete("order:detail:" + orderId);
    }
}