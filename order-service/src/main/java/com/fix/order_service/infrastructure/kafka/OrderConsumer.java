package com.fix.order_service.infrastructure.kafka;

import com.fix.common_service.kafka.consumer.AbstractKafkaConsumer;
import com.fix.common_service.kafka.consumer.RedisIdempotencyChecker;
import com.fix.common_service.kafka.dto.TicketReservedPayload;
import com.fix.common_service.kafka.dto.TicketUpdatedPayload;
import com.fix.common_service.kafka.dto.EventKafkaMessage;
import com.fix.order_service.application.OrderFeignService;
import com.fix.order_service.application.dtos.request.FeignOrderCreateRequest;
import com.fix.order_service.application.dtos.request.FeignTicketReserveDto;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
public class OrderConsumer {

    private final TicketReservedConsumer ticketReservedConsumer;

    /**
     * OrderConsumer 생성자에서 두 개의 이벤트 consumer를 초기화합니다.
     *
     * @param idempotencyChecker Redis 기반 중복 처리 유틸
     * @param orderFeignService 이벤트 기반 주문 처리 서비스
     */
    public OrderConsumer(RedisIdempotencyChecker idempotencyChecker,
                         OrderFeignService orderFeignService,
                         OrderProducer orderProducer) {
        this.ticketReservedConsumer = new TicketReservedConsumer(idempotencyChecker, orderFeignService, orderProducer);
    }

    /**
     * Kafka로부터 TICKET_RESERVED 이벤트를 수신합니다.
     */
    @KafkaListener(topics = "${kafka-topics.ticket.reserved}", containerFactory = "kafkaListenerContainerFactory",
            groupId = "order-service-ticket-reserved-consumer")
    public void consumeTicketReserved(
            ConsumerRecord<String, EventKafkaMessage<TicketReservedPayload>> record,
            @Payload EventKafkaMessage<TicketReservedPayload> message,
            Acknowledgment ack) {

        ticketReservedConsumer.consume(record, message, ack);
    }

    /**
     * TicketReserved 이벤트를 처리하는 내부 Consumer 클래스
     */
    static class TicketReservedConsumer extends AbstractKafkaConsumer<TicketReservedPayload> {

        private final OrderFeignService orderFeignService;
        private final OrderProducer orderProducer;

        public TicketReservedConsumer(RedisIdempotencyChecker idempotencyChecker,
                                      OrderFeignService orderFeignService,
                                      OrderProducer orderProducer) {
            super(idempotencyChecker);
            this.orderFeignService = orderFeignService;
            this.orderProducer = orderProducer;
        }

        /**
         * Reserved 이벤트 처리 메서드
         * - 주문 생성 로직 호출
         * - 로그 기록
         * - 비즈니스 연결부 역할을 하는 함수
         */
        @Override
        protected void processPayload(Object payload) {
            // (1) 메시지를 DTO로 매핑
            TicketReservedPayload reserved = mapPayload(payload, TicketReservedPayload.class);

            log.info("[Kafka] TICKET_RESERVED 처리 시작: userId={}, gameId={}, tickets={}",
                    reserved.getUserId(), reserved.getGameId(), reserved.getTicketDetails().size());

            // (2) TicketDetails → FeignTicketReserveDto 리스트로 변환
            List<FeignTicketReserveDto> ticketDtoList = reserved.getTicketDetails().stream()
                    .map(t -> new FeignTicketReserveDto(
                            t.getTicketId(),
                            reserved.getUserId(),
                            reserved.getGameId(),
                            null,                 // seatId는 없음
                            t.getPrice(),
                            null                  // status도 없음
                    ))
                    .collect(Collectors.toList());
            // (3) FeignOrderCreateRequest로 포장
            FeignOrderCreateRequest request = new FeignOrderCreateRequest(ticketDtoList);
            try {
                // (4) order 생성 서비스 호출
                orderFeignService.createOrderFromTicket(request);
            } catch (Exception e) {
                log.error("❌ 주문 생성 실패 → 실패 이벤트 발행: {}", e.getMessage());
                orderProducer.sendOrderCreationFailedEvent(reserved, e.getMessage());
                throw e; // 필요 시 생략 가능 (consume 종료 목적이면)
            }
        }

        @Override
        protected String getConsumerGroupId() {
            return "order-service-ticket-reserved-consumer";
        }
    }

    /**
     * TicketUpdated 이벤트를 처리하는 내부 Consumer 클래스
     * - 티켓 상태 업데이트와 관련된 이벤트 수신
     * - 현재는 로그만 남기며, 추후 재고 업데이트 로직을 연결 가능 근데 이건 티켓에서 처리하는거??
     */
    static class TicketUpdatedConsumer extends AbstractKafkaConsumer<TicketUpdatedPayload> {

        public TicketUpdatedConsumer(RedisIdempotencyChecker idempotencyChecker) {
            super(idempotencyChecker);
        }

        @Override
        protected void processPayload(Object payload) {
            TicketUpdatedPayload updated = mapPayload(payload, TicketUpdatedPayload.class);

            log.info("[Kafka] TICKET_UPDATED 처리 시작: gameId={}, quantity={}",
                    updated.getGameId(), updated.getQuantity());

            // TODO: 재고 상태 업데이트 로직 등 연결 가능
        }

        @Override
        protected String getConsumerGroupId() {
            return null;
        }
    }
}
