package com.fix.order_service.application;

import com.fix.common_service.kafka.dto.*;
import com.fix.order_service.application.dtos.request.FeignOrderCreateRequest;
import com.fix.order_service.application.dtos.request.FeignTicketReserveDto;
import com.fix.order_service.application.dtos.request.OrderSummaryDto;
import com.fix.order_service.application.exception.OrderException;
import com.fix.order_service.domain.Order;
import com.fix.order_service.domain.OrderStatus;
import com.fix.order_service.domain.repository.OrderRepository;
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
        log.info("주문 생성 시작 - orderId={}, userId={}, gameId={}, ticketCount={}",
                orderId,
                request.getTicketDtoList().get(0).getUserId(),
                request.getTicketDtoList().get(0).getGameId(),
                request.getTicketDtoList().size()
        );

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
            log.info("주문 정보 DB 저장 완료 - orderId={}, userId={}, gameId={}, ticketCount={}",
                    order.getOrderId(), order.getUserId(), order.getGameId(), tickets.size());

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
            OrderCreatedPayload payload = new OrderCreatedPayload(order.getOrderId(), ticketIds, totalPrice);
            orderProducer.sendOrderCreatedEvent(payload.getOrderId().toString(), payload);

        } catch (Exception e) {
            List<FeignTicketReserveDto> tickets = request.getTicketDtoList();

            TicketReservedPayload reservedPayload = new TicketReservedPayload(
                    tickets.stream()
                            .map(t -> new TicketDetailPayload(t.getTicketId(), t.getPrice()))
                            .toList(),
                    tickets.get(0).getUserId(),
                    tickets.get(0).getGameId()
            );
            log.error("주문 생성 처리 중 오류 발생 - orderId={}", orderId);
            orderProducer.sendOrderCreationFailedEvent(reservedPayload, e.getMessage());
            log.info("주문 생성 실패 이벤트(보상 트랜잭션) 발행 완료 - orderId={}, reason={}", orderId, e.getMessage());
            throw e;
        }
    }

/**
 * 결제 성공 기반 주문 완료 처리
 * - 주문 상태 COMPLETED로 변경
 * - Kafka로 주문 완료 이벤트 발행
 * - 실패 시 주문 완료 실패 이벤트 발행
 */
    @Transactional
    public void completeOrder(UUID orderId, List<UUID> ticketIds, int totalPrice) {
        log.info("🎯 주문 완료 처리 시작 - orderId={}, ticketCount={}", orderId, ticketIds.size());
        try {
            // [1] 주문 조회
            Order order = orderRepository.findById(orderId)
                    .orElseThrow(() -> new OrderException(OrderException.OrderErrorType.ORDER_NOT_FOUND));

            // [2] 주문 상태 변경 → COMPLETED
            order.complete();
            log.info("주문 상태 변경(COMPLETED) 완료 - orderId={}, status={}", order.getOrderId(), order.getOrderStatus());

            // [3] 주문 완료 Kafka 이벤트 발행
            OrderCompletedPayload payload = new OrderCompletedPayload(orderId, ticketIds, order.getUserId());
            orderProducer.sendOrderCompletedEvent(orderId.toString(), payload);
            log.info("주문 완료 처리 성공, Kafka 이벤트 발행 완료 - orderId={}", orderId);

        } catch (Exception e) {
            // [4] 예외 발생 시 → 주문 정보 조회 (널 허용)
            Order order = orderRepository.findById(orderId).orElse(null);
            // [5] 주문 완료 실패 Kafka 이벤트 발행
            OrderCompletionFailedPayload failedPayload = new OrderCompletionFailedPayload(
                    ticketIds,
                    order != null ? order.getUserId() : null,
                    order != null ? order.getGameId() : null,
                    orderId,
                    e.getMessage()
            );
            log.error("주문 완료 처리 중 오류 발생 - orderId={}, reason={}", orderId, e.getMessage());
            orderProducer.sendOrderCompletionFailedEvent(orderId.toString(), failedPayload);
            log.info("주문 완료 실패 이벤트(보상 트랜잭션) 발행 완료 - orderId={}, reason={}", orderId, e.getMessage());
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
