package com.fix.event_service.domain.repository;

import com.fix.event_service.domain.model.Event;

import java.util.Optional;
import java.util.UUID;

public interface EventRepository {
    void save(Event event);

    Optional<Event> findById(UUID eventId);
}
