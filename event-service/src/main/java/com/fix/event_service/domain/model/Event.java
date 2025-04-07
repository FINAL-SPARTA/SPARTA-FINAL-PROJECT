package com.fix.event_service.domain.model;

import com.fix.common_service.entity.Basic;
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

    @Embedded
    private EventPeriod eventPeriod;

    @Enumerated(EnumType.STRING)
    private EventStatus status;

    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Reward> rewards = new ArrayList<>();

    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<EventEntry> entries = new ArrayList<>();

    @Builder
    public Event(String eventName, String description, LocalDateTime eventStartAt, LocalDateTime eventEndAt, Integer maxWinners) {
        this.eventId = UUID.randomUUID();
        this.eventName = eventName;
        this.description = description;
        this.eventPeriod = new EventPeriod(eventStartAt, eventEndAt);
        this.maxWinners = maxWinners;
        this.status = EventStatus.PLANNED;
    }

    public static Event createEvent(String eventName, String description, LocalDateTime eventStartAt, LocalDateTime eventEndAt, Integer maxWinners) {
        return Event.builder()
                .eventName(eventName)
                .description(description)
                .eventStartAt(eventStartAt)
                .eventEndAt(eventEndAt)
                .maxWinners(maxWinners)
                .build();
    }

    public void updateEvent(String eventName, String description, LocalDateTime eventStartAt, LocalDateTime eventEndAt, Integer maxWinners) {
        this.eventName = eventName;
        this.description = description;
        this.eventPeriod = new EventPeriod(eventStartAt, eventEndAt);
        this.maxWinners = maxWinners;
    }

    public void startEvent() {
        this.status = EventStatus.ONGOING;
    }

    public void endEvent() {
        this.status = EventStatus.CLOSED;
    }

    public void addEntry(EventEntry entry) {
        this.entries.add(entry);
    }

    public void addReward(Reward reward) {
        this.rewards.add(reward);
        reward.setEvent(this);
    }
}
