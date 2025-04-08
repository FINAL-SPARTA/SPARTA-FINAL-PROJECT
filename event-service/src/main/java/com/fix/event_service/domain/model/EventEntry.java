package com.fix.event_service.domain.model;

import com.fix.common_service.entity.Basic;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@NoArgsConstructor
@Getter
@Entity
@Table(name = "p_event_entry")
public class EventEntry extends Basic {
    @Id
    private UUID entryId;

    @ManyToOne
    @JoinColumn(name = "event_id")
    private Event event;

    private Long userId;

    private Boolean isWinner;

    @Builder
    public EventEntry(Event event, Long userId) {
        this.entryId = UUID.randomUUID();
        this.event = event;
        this.userId = userId;
        this.isWinner = false;
    }

    public static EventEntry createEventEntry(Event event, Long userId) {
        return EventEntry.builder()
                .event(event)
                .userId(userId)
                .build();
    }

    public void setEvent(Event event) {
        this.event = event;
    }

    public void markAsWinner() {
        this.isWinner = true;
    }
}
