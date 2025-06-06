package com.fix.event_service.domain.repository;

import com.fix.event_service.domain.model.Event;
import com.fix.event_service.domain.model.EventStatus;
import org.springframework.data.domain.Page;

import java.util.Optional;
import java.util.UUID;

public interface EventRepository {
    void save(Event event);

    Optional<Event> findById(UUID eventId);

    Page<Event> findAll(int page, int size);

    Page<Event> searchEvents(EventStatus status, String keyword, int page, int size);
}
