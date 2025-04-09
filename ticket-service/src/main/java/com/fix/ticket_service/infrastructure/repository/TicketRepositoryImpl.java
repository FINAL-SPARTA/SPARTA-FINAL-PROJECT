package com.fix.ticket_service.infrastructure.repository;

import com.fix.ticket_service.domain.model.Ticket;
import com.fix.ticket_service.domain.repository.TicketRepository;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public class TicketRepositoryImpl implements TicketRepository {

    @PersistenceContext
    private EntityManager em;
    private final JpaTicketRepository jpaTicketRepository;
    private final JPAQueryFactory qf;

    public TicketRepositoryImpl(JpaTicketRepository jpaTicketRepository, EntityManager em) {
        this.jpaTicketRepository = jpaTicketRepository;
        this.qf = new JPAQueryFactory(em);
    }

    @Override
    public void save(Ticket ticket) {
        jpaTicketRepository.save(ticket);
    }

    @Override
    public Optional<Ticket> findById(UUID ticketId) {
        return jpaTicketRepository.findById(ticketId);
    }

    @Override
    public Page<Ticket> findAllByUserId(Long userId, int page, int size) {
        return jpaTicketRepository.findAllByUserId(userId, PageRequest.of(page, size));
    }
}
