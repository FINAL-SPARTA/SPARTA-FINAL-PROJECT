package com.fix.event_service.domain.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum EventStatus {
    PLANNED("진행 예정"),
    ONGOING("진행중"),
    CLOSED("종료됨");

    private final String description;
}
