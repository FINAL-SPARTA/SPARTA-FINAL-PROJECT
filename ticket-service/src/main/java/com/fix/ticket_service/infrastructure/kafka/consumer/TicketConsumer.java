package com.fix.ticket_service.infrastructure.kafka.consumer;


import com.fix.common_service.kafka.dto.EventKafkaMessage;
import com.fix.common_service.kafka.dto.OrderCancelledPayload;
import com.fix.common_service.kafka.dto.OrderCreatedPayload;
import com.fix.ticket_service.application.dtos.request.TicketSoldRequestDto;
import com.fix.ticket_service.application.service.TicketApplicationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class TicketConsumer {

    private final TicketApplicationService ticketApplicationService;

    // topic 명은 주문에서 이벤트를 발행한다고 가정하고 설정, 결제에서 발행한다면 수정 필요
    @KafkaListener(topics = "order-created-topic", groupId = "ticket-service-group")
    public void consumeOrderCreatedEvent(EventKafkaMessage eventMessage) {
        OrderCreatedPayload payload = (OrderCreatedPayload) eventMessage.getPayload();
        log.info("[Kafka] 주문 생성 이벤트 수신: {}", payload);

        TicketSoldRequestDto requestDto = new TicketSoldRequestDto(payload.getOrderId(), payload.getTicketIds());
        try {
            ticketApplicationService.updateTicketStatus(requestDto);
            log.info("[Kafka] 티켓 판매 상태 업데이트 성공: orderId={}, ticketIds={}",
                requestDto.getOrderId(), requestDto.getTicketIds());
        } catch (Exception e) {
            // TODO: 보상 트랜잭션 구현
            log.info("[Kafka] 티켓 판매 상태 업데이트 실패: orderId={}, ticketIds={}",
                requestDto.getOrderId(), requestDto.getTicketIds());
        }
    }

    @KafkaListener(topics = "order-cancelled-topic", groupId = "ticket-service-group")
    public void consumeOrderCancelledEvent(EventKafkaMessage eventMessage) {
        OrderCancelledPayload payload = (OrderCancelledPayload) eventMessage.getPayload();
        log.info("[Kafka] 주문 취소 이벤트 수신: {}", payload);

        UUID orderId = payload.getOrderId();
        try {
            ticketApplicationService.cancelTicketStatus(orderId);
            log.info("[Kafka] 티켓 취소 상태 업데이트 성공: orderId={}", orderId);
        } catch (Exception e) {
            // TODO: 보상 트랜잭션 구현
            log.info("[Kafka] 티켓 취소 상태 업데이트 실패: orderId={}", orderId);
        }
    }
}
