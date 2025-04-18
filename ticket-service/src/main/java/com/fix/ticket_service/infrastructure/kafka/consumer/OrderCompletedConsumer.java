package com.fix.ticket_service.infrastructure.kafka.consumer;

import com.fix.common_service.kafka.consumer.AbstractKafkaConsumer;
import com.fix.common_service.kafka.consumer.RedisIdempotencyChecker;
import com.fix.common_service.kafka.dto.EventKafkaMessage;
import com.fix.common_service.kafka.dto.OrderCompletedPayload;
import com.fix.ticket_service.application.dtos.request.TicketSoldRequestDto;
import com.fix.ticket_service.application.service.TicketApplicationService;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class OrderCompletedConsumer extends AbstractKafkaConsumer<OrderCompletedPayload> {

    private final TicketApplicationService ticketApplicationService;

    public OrderCompletedConsumer(RedisIdempotencyChecker idempotencyChecker,
                                  TicketApplicationService ticketApplicationService) {
        super(idempotencyChecker);
        this.ticketApplicationService = ticketApplicationService;
    }

    @KafkaListener(topics = "${kafka-topics.order.completed}", groupId = "ticket-service-order-completed-consumer")
    public void listen(ConsumerRecord<String, EventKafkaMessage<OrderCompletedPayload>> record,
                       EventKafkaMessage<OrderCompletedPayload> message,
                       Acknowledgment ack) {
        super.consume(record, message, ack);
    }
    @Override
    protected void processPayload(Object rawPayload) throws IllegalArgumentException {
        OrderCompletedPayload payload = mapPayload(rawPayload, OrderCompletedPayload.class);
        TicketSoldRequestDto requestDto = new TicketSoldRequestDto(payload.getOrderId(), payload.getTicketIds());
        try {
            ticketApplicationService.updateTicketStatus(requestDto);
            log.info("[Kafka] 티켓 판매 상태 업데이트 성공: orderId={}, ticketIds={}",
                requestDto.getOrderId(), requestDto.getTicketIds());
        } catch (IllegalArgumentException e) {
            // 에러 핸들러에 의한 재시도 처리, 보상 트랜잭션은 구현 X
            // 결제 및 주문이 완료되었는데 롤백을 하는건 뭔가 이상함..
            log.error("[Kafka] 티켓 판매 상태 업데이트 실패: orderId={}, ticketIds={}, error={}",
                requestDto.getOrderId(), requestDto.getTicketIds(), e.getMessage());
        }
    }
}
