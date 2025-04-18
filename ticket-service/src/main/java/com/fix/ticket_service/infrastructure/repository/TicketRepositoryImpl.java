package com.fix.ticket_service.infrastructure.repository;

import com.fix.ticket_service.domain.model.QTicket;
import com.fix.ticket_service.domain.model.Ticket;
import com.fix.ticket_service.domain.model.TicketStatus;
import com.fix.ticket_service.domain.repository.TicketRepository;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

import java.util.List;
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

    @Override
    public List<Ticket> findAllById(List<UUID> ticketIds) {
        return jpaTicketRepository.findAllById(ticketIds);
    }

    @Override
    public void delete(Ticket ticket) {
        jpaTicketRepository.delete(ticket);
    }

    @Override
    public void deleteAllByStatus(TicketStatus ticketStatus) {
        jpaTicketRepository.deleteAllByStatus(ticketStatus);
    }

    @Override
    public Page<Ticket> searchTickets(UUID gameId, Long userId, int page, int size) {
        QTicket ticket = QTicket.ticket;

        BooleanBuilder builder = new BooleanBuilder();
        builder.and(ticket.gameId.eq(gameId));

        if (userId != null) {
            builder.and(ticket.userId.eq(userId));
        }

        PageRequest pageable = PageRequest.of(page, size);

        List<Ticket> content = qf
            .selectFrom(ticket)
            .where(builder)
            .orderBy(ticket.soldAt.isNotNull().desc())
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .fetch();

        long total = Optional.ofNullable(
            qf.select(ticket.count())
                .from(ticket)
                .where(builder)
                .fetchOne()).orElse(0L);

        return new PageImpl<>(content, pageable, total);
    }

    @Override
    public List<Ticket> findAllByOrderId(UUID orderId) {
        return jpaTicketRepository.findAllByOrderId(orderId);
    }

    @Override
    public void saveAll(List<Ticket> ticketsToSave) {
        jpaTicketRepository.saveAll(ticketsToSave);
    }

    @Override
    public List<Ticket> findByGameIdAndSeatIdInAndStatusIn(UUID gameId, List<UUID> seatIdsInSection, List<TicketStatus> statusesToFetch) {
        return jpaTicketRepository.findByGameIdAndSeatIdInAndStatusIn(gameId, seatIdsInSection, statusesToFetch);
    }

    @Override
    public void deleteAll(List<Ticket> tickets) {
        jpaTicketRepository.deleteAll(tickets);
    }
}
