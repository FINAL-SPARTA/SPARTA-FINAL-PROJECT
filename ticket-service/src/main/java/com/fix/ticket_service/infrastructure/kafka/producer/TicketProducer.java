package com.fix.ticket_service.infrastructure.kafka.producer;


import com.fix.common_service.kafka.dto.*;
import com.fix.common_service.kafka.producer.KafkaProducerHelper;
import com.fix.ticket_service.application.dtos.request.TicketInfoRequestDto;
import com.fix.ticket_service.domain.model.Ticket;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class TicketProducer {

    private final KafkaProducerHelper kafkaProducerHelper;

    @Value("${kafka-topics.ticket.reserved}")
    private String ticketReservedTopic;
    @Value("${kafka-topics.ticket.sold}")
    private String ticketSoldTopic;
    @Value("${kafka-topics.ticket.cancelled}")
    private String ticketCancelledTopic;
    @Value("${kafka-topics.ticket.reservation.request}")
    private String ticketReservationRequestTopic;
    @Value("${kafka-topics.ticket.reservation.succeeded}")
    private String ticketReservationSucceededTopic;
    @Value("${kafka-topics.ticket.reservation.failed}")
    private String ticketReservationFailedTopic;

    public void sendTicketReservationRequestEvent(
        UUID reservationRequestId, Long userId, String queueToken, UUID gameId, List<TicketInfoRequestDto> seatInfoList) {
        int totalSeats = seatInfoList.size();
        for (TicketInfoRequestDto seatInfo : seatInfoList) {
            // 각 좌석별 Payload 생성
            TicketReservationRequestPayload payload = new TicketReservationRequestPayload(
                reservationRequestId,
                userId,
                queueToken,
                gameId,
                new TicketInfoPayload(seatInfo.getSeatId(), seatInfo.getPrice()),
                seatInfoList.stream()
                    .map(seat -> new TicketInfoPayload(seat.getSeatId(), seat.getPrice()))
                    .toList(), // 원본 좌석 정보
                totalSeats // 전체 좌석 수
            );
            EventKafkaMessage<TicketReservationRequestPayload> eventMessage = new EventKafkaMessage<>(
                "TICKET_RESERVATION_REQUEST", payload);
            // 파티션 키 : gameId:seatId
            String key = gameId.toString() + ":" + seatInfo.getSeatId().toString();

            kafkaProducerHelper.send(ticketReservationRequestTopic, key, eventMessage);
        }
    }

    public void sendTicketReservationSucceededEvent(TicketReservationSucceededPayload payload) {
        EventKafkaMessage<TicketReservationSucceededPayload> eventMessage = new EventKafkaMessage<>(
            "TICKET_RESERVATION_SUCCEEDED", payload);

        String key = payload.getUserId().toString();

        kafkaProducerHelper.send(ticketReservationSucceededTopic, key, eventMessage);
    }

    public void sendTicketReservationFailedEvent(TicketReservationFailedPayload payload) {
        EventKafkaMessage<TicketReservationFailedPayload> eventMessage = new EventKafkaMessage<>(
            "TICKET_RESERVATION_FAILED", payload);

        String key = payload.getUserId().toString();

        kafkaProducerHelper.send(ticketReservationFailedTopic, key, eventMessage);
    }

    public void sendTicketReservedEvent(List<Ticket> tickets, Long userId) {
        if (tickets == null || tickets.isEmpty()) return;

        List<TicketDetailPayload> ticketDetails = tickets.stream()
                .map(ticket -> new TicketDetailPayload(ticket.getTicketId(), ticket.getPrice()))
                .toList();

        TicketReservedPayload payload = new TicketReservedPayload(ticketDetails, userId, tickets.get(0).getGameId());
        EventKafkaMessage<TicketReservedPayload> eventMessage = new EventKafkaMessage<>("TICKET_RESERVED", payload);

        String key = tickets.get(0).getGameId().toString();

        kafkaProducerHelper.send(ticketReservedTopic, key, eventMessage);
    }

    public void sendTicketSoldEvent(UUID gameId, int quantity) {
        TicketUpdatedPayload payload = new TicketUpdatedPayload(gameId, quantity);

        EventKafkaMessage<TicketUpdatedPayload> eventMessage = new EventKafkaMessage<>("TICKET_SOLD", payload);

        String key = gameId.toString();

        kafkaProducerHelper.send(ticketSoldTopic, key, eventMessage);
    }

    public void sendTicketCancelledEvent(UUID gameId, int quantity) {
        TicketUpdatedPayload payload = new TicketUpdatedPayload(gameId, quantity);

        EventKafkaMessage<TicketUpdatedPayload> eventMessage = new EventKafkaMessage<>("TICKET_CANCELLED", payload);

        String key = gameId.toString();

        kafkaProducerHelper.send(ticketCancelledTopic, key, eventMessage);
    }
}
