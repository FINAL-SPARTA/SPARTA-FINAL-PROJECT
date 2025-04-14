package com.fix.ticket_service.infrastructure.kafka.producer;

import com.fix.common_service.dto.EventKafkaMessage;
import com.fix.common_service.dto.TicketReservedPayload;
import com.fix.common_service.dto.TicketUpdatedPayload;
import com.fix.ticket_service.domain.model.Ticket;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class TicketProducer {

    private final KafkaTemplate<String, EventKafkaMessage> kafkaTemplate;

    public void sendTicketReservedEvent(List<Ticket> tickets, Long userId) {
        List<TicketReservedPayload.TicketDetail> ticketDetails = tickets.stream()
                .map(ticket -> new TicketReservedPayload.TicketDetail(ticket.getTicketId(), ticket.getPrice()))
                .toList();
        TicketReservedPayload payload = new TicketReservedPayload(ticketDetails, userId, tickets.get(0).getGameId());

        EventKafkaMessage eventMessage = new EventKafkaMessage("TICKET_RESERVED", payload);
        kafkaTemplate.send("ticket-reserved-topic", eventMessage);
        log.info("[Kafka] TICKET_RESERVED 이벤트 발행: {}", eventMessage);
    }

    public void sendTicketUpdatedEvent(UUID gameId, int quantity) {
        TicketUpdatedPayload payload = new TicketUpdatedPayload(gameId, quantity);

        EventKafkaMessage eventMessage = new EventKafkaMessage("TICKET_UPDATED", payload);
        kafkaTemplate.send("ticket-updated-topic", eventMessage);
        log.info("[Kafka] TICKET_UPDATED 이벤트 발행: {}", eventMessage);
    }
}
