package com.fix.order_serivce.application;

import com.fix.order_serivce.application.dtos.request.OrderSearchCondition;
import com.fix.order_serivce.application.dtos.request.OrderUpdateRequest;
import com.fix.order_serivce.application.dtos.response.OrderDetailResponse;
import com.fix.order_serivce.application.dtos.response.OrderResponse;
import com.fix.order_serivce.domain.Order;
import com.fix.order_serivce.domain.repository.OrderQueryRepository;
import com.fix.order_serivce.domain.repository.OrderRepository;
import com.fix.order_serivce.application.exception.OrderException;
import static com.fix.order_serivce.application.exception.OrderException.OrderErrorType.*;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderQueryRepository orderQueryRepository;

//    단건 조회
    @Transactional(readOnly = true)
    public OrderDetailResponse getOrder(UUID orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderException(ORDER_NOT_FOUND));

        return OrderDetailResponse.builder()
                .orderId(order.getOrderId())
                .userId(order.getUserId())
                .gameId(order.getGameId())
                .peopleCount(order.getPeopleCount())
                .totalPrice(order.getTotalPrice())
                .build();
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
    }

//    주문 삭제 (soft delete)
    @Transactional
    public void deleteOrder(UUID orderId, Long userId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderException(ORDER_NOT_FOUND));
        order.softDelete(userId);
    }
}