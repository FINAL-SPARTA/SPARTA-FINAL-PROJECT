package com.fix.event_service.application.dtos.response;

import com.fix.event_service.domain.model.EventEntry;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@NoArgsConstructor
public class EventEntryResponseDto {
    private UUID entryId;
    private Long userId;
    private Boolean isWinner;

    public EventEntryResponseDto(EventEntry entry) {
        this.entryId = entry.getEntryId();
        this.userId = entry.getUserId();
        this.isWinner = entry.getIsWinner();
    }
}
