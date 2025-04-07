package com.fix.order_serivce.application;

import com.fix.common_service.exception.CustomException;
import com.fix.order_serivce.application.dtos.request.OrderCreateRequest;
import com.fix.order_serivce.application.dtos.response.OrderDetailResponse;
import com.fix.order_serivce.application.dtos.response.TicketInfo;
import com.fix.order_serivce.domain.Order;
import com.fix.order_serivce.domain.OrderStatus;
import com.fix.order_serivce.domain.Ticket;
import com.fix.order_serivce.domain.repository.OrderRepository;
import com.fix.order_serivce.domain.repository.TicketRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final TicketRepository ticketRepository;

    /**
     * 주문 생성 메서드
     * 사용자의 주문 요청을 받아 Order 및 관련 Ticket 데이터를 생성하고 저장합니다.
     * 트랜잭션으로 감싸져 있어 전체 작업이 모두 성공해야 DB에 반영되며,
     * 중간 실패 시 전체 롤백됩니다.
     *
     * @param request 주문 생성 요청 정보 (사용자 ID, 경기 ID, 인원 수, 좌석 ID 리스트 등)
     * @return 생성된 주문의 ID(UUID)
     */
    @Transactional
    public UUID createOrder(OrderCreateRequest request) {
        // 1. 주문 엔티티 생성
        // - 사용자 ID, 경기 ID, 상태(CREATED), 인원 수, 좌석 수를 기반으로 Order 객체 생성
        Order order = Order.create(
                request.getUserId(),
                request.getGameId(),
                OrderStatus.CREATED,                       // 초기 주문 상태는 CREATED
                request.getPeopleCount(),                 // 인원 수
                request.getSeatIds().size()               // 좌석 수 = 티켓 수
        );
        // 2. 주문 저장
        // - 생성된 주문을 order 테이블에 저장
        orderRepository.save(order);

        // 3. 티켓 생성 및 저장
        // - 각 좌석 ID에 대해 티켓 생성
        // - 하나의 주문(orderId)에 여러 티켓이 연결됨
        List<Ticket> tickets = request.getSeatIds().stream()
                .map(seatId -> Ticket.create(order.getOrderId(), seatId, request.getTotalPrice()))
                .collect(Collectors.toList());

        // - 생성된 티켓 리스트를 DB에 일괄 저장
        ticketRepository.saveAll(tickets);

        // 4. Kafka 발행 (추후 구현 예정)
        // - 예: 예매 확정 알림, 좌석 차감 이벤트 등 외부 시스템 연동을 위한 이벤트 발행 가능

        // 5. 생성된 주문 ID 반환
        return order.getOrderId();
    }

    @Transactional(readOnly = true)
    public OrderDetailResponse getOrder(UUID orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new CustomException("ORDER_NOT_FOUND", "주문 정보를 찾을 수 없습니다.", HttpStatus.NOT_FOUND));

        List<TicketInfo> tickets = ticketRepository.findAllByOrderId(orderId).stream()
                .map(ticket -> TicketInfo.builder()
                        .ticketId(ticket.getTicketId())
                        .seatId(ticket.getSeatId())
                        .price(ticket.getPrice())
                        .build())
                .toList();

        return OrderDetailResponse.builder()
                .orderId(order.getOrderId())
                .userId(order.getUserId())
                .gameId(order.getGameId())
                .peopleCount(order.getPeopleCount())
                .totalCount(order.getTotalCount())
                .tickets(tickets)
                .build();
    }
}