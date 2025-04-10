package com.fix.ticket_service.domain.repository;

import com.fix.ticket_service.domain.model.Ticket;
import com.fix.ticket_service.domain.model.TicketStatus;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TicketRepository {
    void save(Ticket ticket);

    Optional<Ticket> findById(UUID ticketId);

    Page<Ticket> findAllByUserId(Long userId, int page, int size);

    List<Ticket> findAllById(List<UUID> ticketIds);

    void delete(Ticket ticket);

    void deleteAllByStatus(TicketStatus ticketStatus);

    Page<Ticket> searchTickets(UUID gameId, Long userId, int page, int size);

    List<Ticket> findAllByOrderId(UUID orderId);

    List<Ticket> findBySeatIdInAndStatusIn(List<UUID> seatIds, List<TicketStatus> reserved);
}
