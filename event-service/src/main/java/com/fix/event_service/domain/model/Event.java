package com.fix.event_service.domain.model;

import com.fix.common_service.entity.Basic;
import com.fix.event_service.application.exception.EventException;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Where;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@NoArgsConstructor
@Getter
@Entity
@Table(name = "p_event")
@Where(clause = "is_deleted = false")
public class Event extends Basic {
    @Id
    private UUID eventId;

    private String eventName;
    private String description;
    private Integer maxWinners;
    private Integer requiredPoints;

    @Embedded
    private EventPeriod eventPeriod;

    @Enumerated(EnumType.STRING)
    private EventStatus status;

    @OneToOne(mappedBy = "event", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Reward reward;

    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<EventEntry> entries = new ArrayList<>();

    @Builder
    public Event(String eventName, String description, LocalDateTime eventStartAt,
                 LocalDateTime eventEndAt, Integer maxWinners, Integer requiredPoints) {
        this.eventId = UUID.randomUUID();
        this.eventName = eventName;
        this.description = description;
        this.eventPeriod = new EventPeriod(eventStartAt, eventEndAt);
        this.maxWinners = maxWinners;
        this.requiredPoints = requiredPoints;
        this.status = EventStatus.PLANNED;
    }

    public static Event createEvent(String eventName, String description, LocalDateTime eventStartAt,
                                    LocalDateTime eventEndAt, Integer maxWinners, Integer requiredPoints) {
        return Event.builder()
                .eventName(eventName)
                .description(description)
                .eventStartAt(eventStartAt)
                .eventEndAt(eventEndAt)
                .maxWinners(maxWinners)
                .requiredPoints(requiredPoints)
                .build();
    }

    public void updateEvent(String eventName, String description, LocalDateTime eventStartAt,
                            LocalDateTime eventEndAt, Integer maxWinners, Integer requiredPoints, Reward reward) {
        this.eventName = eventName;
        this.description = description;
        this.eventPeriod = new EventPeriod(eventStartAt, eventEndAt);
        this.maxWinners = maxWinners;
        this.requiredPoints = requiredPoints;
        this.reward = reward;
    }

    public void checkUpdatable() {
        if (this.status != EventStatus.PLANNED) {
            throw new EventException(EventException.EventErrorType.EVENT_CANNOT_UPDATE);
        }
    }

    public void checkDeletable() {
        if (this.status == EventStatus.ONGOING) {
            throw new EventException(EventException.EventErrorType.EVENT_CANNOT_DELETE);
        }
    }

    public void startEvent() {
        this.status = EventStatus.ONGOING;
    }

    public void endEvent() {
        this.status = EventStatus.CLOSED;
    }

    public void addEntry(EventEntry entry) {
        this.entries.add(entry);
        entry.setEvent(this);
    }

    public void addReward(Reward reward) {
        this.reward = reward;
        reward.setEvent(this);
    }

    public void isEventOpenForApplication() {
        LocalDateTime now = LocalDateTime.now();
        boolean isOngoing = this.status == EventStatus.ONGOING;
        boolean isAfterStart = now.isAfter(eventPeriod.getEventStartAt());
        boolean isBeforeEnd = now.isBefore(eventPeriod.getEventEndAt());

        if (!isOngoing || !isAfterStart || !isBeforeEnd) {
            throw new EventException(EventException.EventErrorType.EVENT_NOT_OPEN_FOR_APPLY);
        }
    }

    @Override
    public void softDelete(Long userId) {
        super.softDelete(userId);

        if (this.entries != null) {
            for (EventEntry entry : this.entries) {
                entry.softDelete(userId);
            }
        }

        if (this.reward != null) {
            this.reward.softDelete(userId);
        }
    }

    public void removeEntry(EventEntry eventEntry) {
        if (this.entries != null) {
            this.entries.remove(eventEntry);
        }
    }
}
