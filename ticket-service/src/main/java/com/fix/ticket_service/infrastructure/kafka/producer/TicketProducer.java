package com.fix.ticket_service.infrastructure.kafka.producer;


import com.fix.common_service.kafka.dto.EventKafkaMessage;
import com.fix.common_service.kafka.dto.TicketReservedPayload;
import com.fix.common_service.kafka.dto.TicketUpdatedPayload;
import com.fix.common_service.kafka.producer.KafkaProducerHelper;
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

    public void sendTicketReservedEvent(List<Ticket> tickets, Long userId) {
        List<TicketReservedPayload.TicketDetail> ticketDetails = tickets.stream()
                .map(ticket -> new TicketReservedPayload.TicketDetail(ticket.getTicketId(), ticket.getPrice()))
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
