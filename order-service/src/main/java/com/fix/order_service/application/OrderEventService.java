package com.fix.order_service.application;

import com.fix.common_service.kafka.dto.TicketReservedPayload;
import com.fix.order_service.application.exception.OrderException;
import com.fix.order_service.domain.Order;
import com.fix.order_service.domain.OrderStatus;
import com.fix.order_service.domain.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderEventService {

    private final OrderRepository orderRepository;

    /**
     * Kafka로부터 수신한 TicketReservedPayload를 기반으로 주문을 생성합니다.
     *      * [1] ticket-service에서 예약된 티켓 리스트를 포함한 이벤트 수신
     *      * [2] 유효성 검사 및 주문 생성
     *      * [3] 주문 저장 및 로그 출력
     */
    @Transactional
    public UUID createOrderFromReservedEvent(TicketReservedPayload payload) {
        // [1] 유효성 검사
        if (payload.getTicketDetails() == null || payload.getTicketDetails().isEmpty()) {
            throw new OrderException(OrderException.OrderErrorType.TICKET_NOT_FOUND);
        }
        // [2] 가격 계산
        int totalPrice = payload.getTicketDetails().stream()
                .mapToInt(TicketReservedPayload.TicketDetail::getPrice)
                .sum();
        // [3] 주문 생성
        Order order = Order.create(
                payload.getUserId(),
                payload.getGameId(),
                OrderStatus.CREATED,
                payload.getTicketDetails().size(),
                totalPrice
        );
        // [4] 주문 저장
        orderRepository.save(order);

        log.info("[Kafka] 주문 생성 완료 (ReservedEvent). orderId={}, gameId={}, userId={}",
                order.getOrderId(), order.getGameId(), order.getUserId());

        return order.getOrderId();
    }


    /**
     * (SAGA 보상 트랜잭션 예정)
     * 예: 티켓 예약 실패 시 주문을 취소하는 처리.
     * 해당 메서드는 다음 스프린트에서 사용 예정이며 현재는 로그 출력만 수행.
     *
     * @param orderId 취소할 주문 ID
     */
    // TODO: 이후 SAGA 보상 트랜잭션 대응 메서드
    @Transactional
    public void cancelOrderForReservationFailure(UUID orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderException(OrderException.OrderErrorType.ORDER_NOT_FOUND));

        order.cancel();

        log.info("[Kafka] 주문 예약 실패로 인한 주문 취소 처리 완료. orderId={}", orderId);
    }
}
