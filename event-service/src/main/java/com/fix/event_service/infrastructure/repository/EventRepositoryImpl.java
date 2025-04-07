package com.fix.event_service.infrastructure.repository;

import com.fix.event_service.domain.model.Event;
import com.fix.event_service.domain.model.EventStatus;
import com.fix.event_service.domain.model.QEvent;
import com.fix.event_service.domain.repository.EventRepository;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
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

    @Override
    public Page<Event> findAll(int page, int size) {
        return jpaEventRepository.findAll(PageRequest.of(page, size));
    }

    @Override
    public Page<Event> searchEvents(EventStatus status, String keyword, int page, int size) {
        QEvent event = QEvent.event;

        BooleanBuilder builder = new BooleanBuilder();
        builder.and(event.status.eq(status));

        if (keyword != null && !keyword.isEmpty()) {
            builder.and(event.eventName.containsIgnoreCase(keyword));
        }

        Pageable pageable = PageRequest.of(page, size);

        List<Event> content = qf
                .selectFrom(event)
                .where(builder)
                .orderBy(event.eventPeriod.eventStartAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        long total = Optional.ofNullable(
            qf.select(event.count())
            .from(event)
            .where(builder)
            .fetchOne()).orElse(0L);

        return new PageImpl<>(content, pageable, total);
    }
}
