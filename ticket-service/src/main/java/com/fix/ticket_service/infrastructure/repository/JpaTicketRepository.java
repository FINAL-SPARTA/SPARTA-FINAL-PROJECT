package com.fix.ticket_service.infrastructure.repository;

import com.fix.ticket_service.domain.model.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface JpaTicketRepository extends JpaRepository<Ticket, UUID> {
}
