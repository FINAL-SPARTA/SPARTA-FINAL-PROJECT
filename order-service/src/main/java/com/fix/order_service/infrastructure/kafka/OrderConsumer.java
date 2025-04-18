package com.fix.order_service.infrastructure.kafka;

import com.fix.common_service.kafka.consumer.AbstractKafkaConsumer;
import com.fix.common_service.kafka.consumer.RedisIdempotencyChecker;
import com.fix.common_service.kafka.dto.TicketReservedPayload;
import com.fix.common_service.kafka.dto.TicketUpdatedPayload;
import com.fix.common_service.kafka.dto.EventKafkaMessage;
import com.fix.order_service.application.OrderEventService;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class OrderConsumer {

    private final TicketReservedConsumer ticketReservedConsumer;
    private final TicketUpdatedConsumer ticketUpdatedConsumer;

    /**
     * OrderConsumer 생성자에서 두 개의 이벤트 consumer를 초기화합니다.
     *
     * @param idempotencyChecker Redis 기반 중복 처리 유틸
     * @param orderEventService 이벤트 기반 주문 처리 서비스
     */
    public OrderConsumer(RedisIdempotencyChecker idempotencyChecker, OrderEventService orderEventService) {
        this.ticketReservedConsumer = new TicketReservedConsumer(idempotencyChecker, orderEventService);
        this.ticketUpdatedConsumer = new TicketUpdatedConsumer(idempotencyChecker);
    }

    /**
     * Kafka로부터 TICKET_RESERVED 이벤트를 수신합니다.
     */
    @KafkaListener(topics = "${kafka-topics.ticket.reserved}", containerFactory = "kafkaListenerContainerFactory")
    public void consumeTicketReserved(
            ConsumerRecord<String, EventKafkaMessage<TicketReservedPayload>> record,
            @Payload EventKafkaMessage<TicketReservedPayload> message,
            Acknowledgment ack) {

        ticketReservedConsumer.consume(record, message, ack);
    }

    /**
     * Kafka로부터 TICKET_UPDATED 이벤트를 수신합니다.
     */
    @KafkaListener(topics = "${kafka-topics.ticket.updated}", containerFactory = "kafkaListenerContainerFactory")
    public void consumeTicketUpdated(
            ConsumerRecord<String, EventKafkaMessage<TicketUpdatedPayload>> record,
            @Payload EventKafkaMessage<TicketUpdatedPayload> message,
            Acknowledgment ack) {

        ticketUpdatedConsumer.consume(record, message, ack);
    }

    /**
     * TicketReserved 이벤트를 처리하는 내부 Consumer 클래스
     */
    static class TicketReservedConsumer extends AbstractKafkaConsumer<TicketReservedPayload> {

        private final OrderEventService orderEventService;

        public TicketReservedConsumer(RedisIdempotencyChecker idempotencyChecker, OrderEventService orderEventService) {
            super(idempotencyChecker);
            this.orderEventService = orderEventService;
        }

        /**
         * Reserved 이벤트 처리 메서드
         * - 주문 생성 로직 호출
         * - 로그 기록
         */
        @Override
        protected void processPayload(Object payload) {
            TicketReservedPayload reserved = mapPayload(payload, TicketReservedPayload.class);

            log.info("[Kafka] TICKET_RESERVED 처리 시작: userId={}, gameId={}, tickets={}",
                    reserved.getUserId(), reserved.getGameId(), reserved.getTicketDetails().size());

            orderEventService.createOrderFromReservedEvent(reserved);
        }
    }

    /**
     * TicketUpdated 이벤트를 처리하는 내부 Consumer 클래스
     */
    static class TicketUpdatedConsumer extends AbstractKafkaConsumer<TicketUpdatedPayload> {

        public TicketUpdatedConsumer(RedisIdempotencyChecker idempotencyChecker) {
            super(idempotencyChecker);
        }

        /**
         * Updated 이벤트 처리 메서드
         * - 현재는 로그 출력만 수행
         */
        @Override
        protected void processPayload(Object payload) {
            TicketUpdatedPayload updated = mapPayload(payload, TicketUpdatedPayload.class);

            log.info("[Kafka] TICKET_UPDATED 처리 시작: gameId={}, quantity={}",
                    updated.getGameId(), updated.getQuantity());

            // TODO: 재고 상태 업데이트 로직 등 연결 가능
        }
    }
}
