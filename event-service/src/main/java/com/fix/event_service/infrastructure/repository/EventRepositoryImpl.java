package com.fix.event_service.infrastructure.repository;

import com.fix.event_service.domain.model.Event;
import com.fix.event_service.domain.repository.EventRepository;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import java.util.Optional;
import java.util.UUID;

public class EventRepositoryImpl implements EventRepository {

    @PersistenceContext
    private EntityManager em;
    private final JpaEventRepository jpaEventRepository;
    private final JPAQueryFactory qf;

    public EventRepositoryImpl(JpaEventRepository jpaEventRepository, EntityManager em) {
        this.jpaEventRepository = jpaEventRepository;
        this.qf = new JPAQueryFactory(em);
    }

    @Override
    public void save(Event event) {
        jpaEventRepository.save(event);
    }

    @Override
    public Optional<Event> findById(UUID eventId) {
        return jpaEventRepository.findById(eventId);
    }
}
