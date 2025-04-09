package com.fix.event_service.domain.model;

import com.fix.event_service.application.exception.EventException;
import jakarta.persistence.Embeddable;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Embeddable
public class EventPeriod {
    private LocalDateTime eventStartAt;
    private LocalDateTime eventEndAt;

    protected EventPeriod() {
        this.eventStartAt = null;
        this.eventEndAt = null;
    }

    public EventPeriod(LocalDateTime eventStartAt, LocalDateTime eventEndAt) {
        LocalDateTime now = LocalDateTime.now();
        if (eventStartAt == null || eventEndAt == null) {
            throw new EventException(EventException.EventErrorType.EVENT_INVALID_PERIOD);
        }
        if (eventStartAt.isAfter(eventEndAt)) {
            throw new EventException(EventException.EventErrorType.EVENT_INVALID_PERIOD);
        }
        if (eventStartAt.isBefore(now)) {
            throw new EventException(EventException.EventErrorType.EVENT_INVALID_PERIOD);
        }
        this.eventStartAt = eventStartAt;
        this.eventEndAt = eventEndAt;
    }
}
