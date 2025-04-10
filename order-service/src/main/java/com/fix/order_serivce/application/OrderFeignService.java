package com.fix.order_serivce.application;

import com.fix.order_serivce.application.dtos.request.FeignOrderCreateRequest;
import com.fix.order_serivce.application.dtos.request.FeignTicketReserveDto;
import com.fix.order_serivce.application.exception.OrderException;
import com.fix.order_serivce.domain.Order;
import com.fix.order_serivce.domain.OrderStatus;
import com.fix.order_serivce.domain.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

import static com.fix.order_serivce.application.exception.OrderException.OrderErrorType.INVALID_REQUEST;

@Service
@RequiredArgsConstructor
public class OrderFeignService {

    private final OrderRepository orderRepository;

    @Transactional
    public void createOrderFromTicket(FeignOrderCreateRequest request) {
        List<FeignTicketReserveDto> tickets = request.getTicketDtoList();

        if (tickets == null || tickets.isEmpty()) {
            throw new OrderException(INVALID_REQUEST);
        }

        // 모든 티켓은 동일한 userId/gameId를 가진다고 가정
        Long userId = tickets.get(0).getUserId();
        UUID gameId = tickets.get(0).getGameId();

        // 주문 생성
        Order order = Order.create(
                UUID.fromString(userId.toString()), // Order 엔티티는 UUID userId 사용
                gameId,
                OrderStatus.CREATED,
                tickets.size(), // peopleCount
                tickets.size()  // totalCount
        );

        orderRepository.save(order);

        // TODO: Kafka 이벤트 발행 (OrderCreated)
        // TODO: ticket-service 에 티켓 상태 SOLD로 변경 요청
    }
}

//ticket-service
//  └── Feign 요청 (POST /api/v1/orders/feign)
//        ↓
//order-service
//  └── OrderFeignController → OrderFeignService
//        ↓
//                └── 주문 정보 저장 (OrderRepository)
//        ↓
//                └── (TODO: Kafka 발행 + 티켓 상태 변경 요청 예정)