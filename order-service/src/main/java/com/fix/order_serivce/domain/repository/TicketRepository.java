package com.fix.order_serivce.domain.repository;

import com.fix.order_serivce.domain.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface TicketRepository extends JpaRepository<Ticket, UUID> {
    List<Ticket> findAllByOrderId(UUID orderId);
}