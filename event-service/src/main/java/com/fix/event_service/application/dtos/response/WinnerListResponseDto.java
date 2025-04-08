package com.fix.event_service.application.dtos.response;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Getter
@NoArgsConstructor
public class WinnerListResponseDto {
    private UUID eventId;
    private List<Long> winners;
    private Integer totalWinners;
    private Integer remainingReward;

    public WinnerListResponseDto(UUID eventId, List<Long> winnerUserIds, int winnerCount, int remainingReward) {
        this.eventId = eventId;
        this.winners = winnerUserIds;
        this.totalWinners = winnerCount;
        this.remainingReward = remainingReward;
    }
}
