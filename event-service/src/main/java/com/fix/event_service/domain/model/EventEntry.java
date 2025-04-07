package com.fix.event_service.domain.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@NoArgsConstructor
@Getter
@Entity
@Table(name = "p_event_entry")
public class EventEntry {
    @Id
    private UUID entryId;

    @ManyToOne
    @JoinColumn(name = "event_id")
    private Event event;

    private Long userId;

    private Boolean isWinner;

    public EventEntry(Event event, Long userId) {
        this.entryId = UUID.randomUUID();
        this.event = event;
        this.userId = userId;
        this.isWinner = false;
    }

    public void markAsWinner() {
        this.isWinner = true;
    }
}
