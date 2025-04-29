package com.fix.ticket_service.infrastructure.kafka.consumer;

import com.fix.common_service.kafka.consumer.AbstractKafkaConsumer;
import com.fix.common_service.kafka.consumer.RedisIdempotencyChecker;
import com.fix.common_service.kafka.dto.EventKafkaMessage;
import com.fix.common_service.kafka.dto.TicketReservationRequestPayload;
import com.fix.ticket_service.application.service.TicketApplicationService;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class TicketReservationRequestConsumer extends AbstractKafkaConsumer<TicketReservationRequestPayload> {

    private final TicketApplicationService ticketApplicationService;
    private static final String CONSUMER_GROUP_ID = "ticket-service-reservation-request-consumer";

    public TicketReservationRequestConsumer(RedisIdempotencyChecker idempotencyChecker,
                                            TicketApplicationService ticketApplicationService) {
        super(idempotencyChecker);
        this.ticketApplicationService = ticketApplicationService;
    }

    @KafkaListener(topics = "${kafka-topics.ticket.reservation.request}", groupId = CONSUMER_GROUP_ID, concurrency = "10")
    public void listen(ConsumerRecord<String, EventKafkaMessage<TicketReservationRequestPayload>> record,
                       EventKafkaMessage<TicketReservationRequestPayload> message,
                       Acknowledgment ack) {
        super.consume(record, message, ack);
    }

    @Override
    protected void processPayload(Object payload) {
        TicketReservationRequestPayload reservationRequestPayload = mapPayload(payload, TicketReservationRequestPayload.class);
        try {
            ticketApplicationService.processReservation(reservationRequestPayload);
            log.info("[Kafka] 티켓 예약 요청 처리 성공: reservationRequestId={}", reservationRequestPayload.getReservationRequestId());
        } catch (Exception e) {
            // 에러 핸들러에 의한 재시도 처리
            log.error("[Kafka] 티켓 예약 요청 처리 실패: reservationRequestId={}, error={}", reservationRequestPayload.getReservationRequestId(), e.getMessage());
            throw e;
        }
    }

    @Override
    protected String getConsumerGroupId() {
        return CONSUMER_GROUP_ID;
    }
}
