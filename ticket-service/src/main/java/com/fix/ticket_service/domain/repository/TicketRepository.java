package com.fix.ticket_service.domain.repository;

import com.fix.ticket_service.domain.model.Ticket;

import java.util.Optional;
import java.util.UUID;

public interface TicketRepository {
    void save(Ticket ticket);

    Optional<Ticket> findById(UUID ticketId);
}
