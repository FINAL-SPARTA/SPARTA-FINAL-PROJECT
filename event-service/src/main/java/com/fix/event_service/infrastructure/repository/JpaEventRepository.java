package com.fix.event_service.infrastructure.repository;

import com.fix.event_service.domain.model.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface JpaEventRepository extends JpaRepository<Event, UUID> {
}
