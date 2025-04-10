package com.fix.event_service.application.dtos.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class EventCreateRequestDto {
    private String eventName;
    private String description;
    private LocalDateTime eventStartAt;
    private LocalDateTime eventEndAt;
    private Integer maxWinners;
    private Integer requiredPoints;
    private RewardRequestDto reward;
}
