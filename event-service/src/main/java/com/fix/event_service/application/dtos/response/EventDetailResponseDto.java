package com.fix.event_service.application.dtos.response;

import com.fix.event_service.domain.model.Event;
import com.fix.event_service.domain.model.EventStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@NoArgsConstructor
public class EventDetailResponseDto {
    private UUID eventId;
    private String eventName;
    private String description;
    private String eventStartAt;
    private String eventEndAt;
    private Integer maxWinners;
    private EventStatus status;
    private RewardResponseDto reward;

    public EventDetailResponseDto(Event event) {
        this.eventId = event.getEventId();
        this.eventName = event.getEventName();
        this.description = event.getDescription();
        this.eventStartAt = event.getEventPeriod().getEventStartAt().toString();
        this.eventEndAt = event.getEventPeriod().getEventEndAt().toString();
        this.maxWinners = event.getMaxWinners();
        this.status = event.getStatus();
        this.reward = new RewardResponseDto(event.getReward());
    }
}
