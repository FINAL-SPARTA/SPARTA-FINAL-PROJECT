package com.fix.order_serivce.application;

import com.fix.order_serivce.application.dtos.request.FeignOrderCreateRequest;
import com.fix.order_serivce.application.dtos.request.FeignTicketReserveDto;
import com.fix.order_serivce.application.dtos.request.FeignTicketSoldRequest;
import com.fix.order_serivce.application.exception.OrderException;
import com.fix.order_serivce.domain.Order;
import com.fix.order_serivce.domain.OrderStatus;
import com.fix.order_serivce.domain.repository.OrderRepository;
import com.fix.order_serivce.infrastructure.client.TicketClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

import static com.fix.order_serivce.application.exception.OrderException.OrderErrorType.INVALID_REQUEST;
import static com.fix.order_serivce.application.exception.OrderException.OrderErrorType.ORDER_NOT_FOUND;

@Service
@RequiredArgsConstructor
public class OrderFeignService {

    private final OrderRepository orderRepository;
    private final TicketClient ticketClient;

    /**
     * ticket-service에서 예약된 티켓 리스트를 전달받아 주문을 생성하고,
     * 해당 티켓들의 상태를 SOLD로 변경 요청함
     *
     * @param request 예약된 티켓 리스트 (userId, gameId, seatId, price 포함)
     */
    @Transactional
    public void createOrderFromTicket(FeignOrderCreateRequest request) {
        List<FeignTicketReserveDto> tickets = request.getTicketDtoList();

        // [1] 유효성 검사
        if (tickets == null || tickets.isEmpty()) {
            throw new OrderException(INVALID_REQUEST);
        }

        // [2] 공통 필드 추출 (모든 티켓이 같은 userId/gameId를 가진다고 가정)
        Long userId = tickets.get(0).getUserId();
        UUID gameId = tickets.get(0).getGameId();

        // [3] 주문 생성
        Order order = Order.create(
                userId,  // Order Entity는 UUID 기반
                gameId,
                OrderStatus.CREATED,
                tickets.size(), // peopleCount
                tickets.size()  // totalCount
        );

        // [4] 주문 저장
        orderRepository.save(order);

        // [5] 티켓 ID 추출
        List<UUID> ticketIds = tickets.stream()
                .map(FeignTicketReserveDto::getTicketId)
                .toList();

        // [6] 티켓 상태 SOLD로 변경 요청
        ticketClient.updateTicketStatus(new FeignTicketSoldRequest(order.getOrderId(), ticketIds)); // ✅ 통합된 호출출
        // [7] (선택) Kafka OrderCreated 이벤트 발행 예정
    }

    @Transactional
    public void cancelOrderFromTicket(UUID orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderException(ORDER_NOT_FOUND));

        // 주문 상태 변경 (soft delete 아님)
        order.cancel();

        // 티켓 상태도 CANCELLED로 변경 요청
        ticketClient.cancelTicketStatus(orderId);
    }
}
//예약된 티켓 → 주문 생성 → 상태 변경
//ticket-service
//  └── Feign 요청 (POST /api/v1/orders/feign)
//        ↓
//order-service
//  └── OrderFeignController → OrderFeignService
//        ↓
//                └── 주문 정보 저장 (OrderRepository)
//        ↓
//                └── (TODO: Kafka 발행 + 티켓 상태 변경 요청 예정)