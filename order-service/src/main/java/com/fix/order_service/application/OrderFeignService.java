package com.fix.order_service.application;

import com.fix.common_service.kafka.dto.*;
import com.fix.order_service.application.dtos.request.FeignOrderCreateRequest;
import com.fix.order_service.application.dtos.request.FeignTicketReserveDto;
import com.fix.order_service.application.dtos.request.FeignTicketSoldRequest;
import com.fix.order_service.application.dtos.request.OrderSummaryDto;
import com.fix.order_service.application.exception.OrderException;
import com.fix.order_service.domain.Order;
import com.fix.order_service.domain.OrderStatus;
import com.fix.order_service.domain.repository.OrderRepository;
import com.fix.order_service.infrastructure.client.TicketClient;
import com.fix.order_service.infrastructure.kafka.OrderProducer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderFeignService {

    private final OrderRepository orderRepository;
    private final OrderHistoryRedisService orderHistoryRedisService;
    private final OrderProducer orderProducer;

    /**
     * ticket-service에서 예약된 티켓 리스트를 전달받아 주문을 생성하고,
     * 해당 티켓들의 상태를 SOLD로 변경 요청함
     *
     * @param request 예약된 티켓 리스트 (userId, gameId, seatId, price 포함)
     */
    @Transactional
    public void createOrderFromTicket(FeignOrderCreateRequest request) {
        UUID orderId = UUID.randomUUID();

        try {
            List<FeignTicketReserveDto> tickets = request.getTicketDtoList();

            // [1] 유효성 검사
            if (tickets == null || tickets.isEmpty()) {
                throw new OrderException(OrderException.OrderErrorType.TICKET_NOT_FOUND);
            }

            // [2] 공통 필드 추출 (모든 티켓이 같은 userId/gameId를 가진다고 가정)
            Long userId = tickets.get(0).getUserId();
            UUID gameId = tickets.get(0).getGameId();

            // ✅ 총 가격 계산 (price 합산)
            int totalPrice = tickets.stream()
                    .mapToInt(FeignTicketReserveDto::getPrice)
                    .sum();

            // [3] 주문 생성
            Order order = Order.builder()
                    .orderId(orderId)
                    .userId(userId)
                    .gameId(gameId)
                    .orderStatus(OrderStatus.CREATED)
                    .peopleCount(tickets.size())
                    .totalPrice(totalPrice)
                    .build();

            // [4] 주문 저장
            orderRepository.save(order);

            // 유저별 최근 주문 내역 Redis에 저장
            orderHistoryRedisService.saveRecentOrder(userId, OrderSummaryDto.builder()
                    .orderId(order.getOrderId())
                    .gameId(order.getGameId())
                    .peopleCount(order.getPeopleCount())
                    .totalPrice(order.getTotalPrice())
                    .createdAt(order.getCreatedAt())
                    .build());

            // [5] 티켓 ID 추출
            List<UUID> ticketIds = tickets.stream()
                    .map(FeignTicketReserveDto::getTicketId)
                    .toList();

            // [6] 티켓 상태 SOLD로 변경 요청
//            ticketClient.updateTicketStatus(new FeignTicketSoldRequest(order.getOrderId(), ticketIds));

            // [7] (선택) Kafka OrderCreated 이벤트 발행 예정
            OrderCreatedPayload payload = new OrderCreatedPayload(order.getOrderId(), ticketIds);
            orderProducer.sendOrderCreatedEvent(payload.getOrderId().toString(), payload);

        } catch (Exception e) {
            List<FeignTicketReserveDto> tickets = request.getTicketDtoList();

            TicketReservedPayload reservedPayload = new TicketReservedPayload(
                    tickets.stream()
                            .map(t -> new TicketReservedPayload.TicketDetail(t.getTicketId(), t.getPrice()))
                            .toList(),
                    tickets.get(0).getUserId(),
                    tickets.get(0).getGameId()
            );

            orderProducer.sendOrderCreationFailedEvent(reservedPayload, e.getMessage());
            throw e;
        }
    }

    @Transactional
    public void completeOrder(UUID orderId, List<UUID> ticketIds) {
        try {
            Order order = orderRepository.findById(orderId)
                    .orElseThrow(() -> new OrderException(OrderException.OrderErrorType.ORDER_NOT_FOUND));

            order.complete();

            OrderCompletedPayload payload = new OrderCompletedPayload(order.getOrderId(), ticketIds);
            orderProducer.sendOrderCompletedEvent(payload.getOrderId().toString(), payload);

        } catch (Exception e) {
            Order order = orderRepository.findById(orderId).orElse(null);

            OrderCompletionFailedPayload failedPayload = new OrderCompletionFailedPayload(
                    ticketIds,
                    order != null ? order.getUserId() : null,
                    order != null ? order.getGameId() : null,
                    orderId,
                    e.getMessage()
            );
            orderProducer.sendOrderCompletionFailedEvent(orderId.toString(), failedPayload);
            throw e;
        }
    }
}
/**
 * 예약된 티켓 기반 주문 생성
 * - 주문 정보 저장 (OrderRepository)
 * - 주문 요약 Redis 저장
 * - 티켓 상태 SOLD로 변경 (ticket-service 연동)
 * - 주문 생성 Kafka 이벤트 발행 (성공 시)
 * - 실패 시 Kafka 주문 생성 실패 이벤트 발행
 */
