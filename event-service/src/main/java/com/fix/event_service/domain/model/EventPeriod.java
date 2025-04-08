package com.fix.event_service.domain.model;

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
            throw new IllegalArgumentException("이벤트 시작 및 종료 시간은 null일 수 없습니다.");
        }
        if (eventStartAt.isAfter(eventEndAt)) {
            throw new IllegalArgumentException("이벤트 시작 시간은 종료 시간보다 늦을 수 없습니다.");
        }
        if (eventStartAt.isBefore(now)) {
            throw new IllegalArgumentException("이벤트 시작 시간은 현재 시간보다 과거일 수 없습니다.");
        }
        this.eventStartAt = eventStartAt;
        this.eventEndAt = eventEndAt;
    }
}
