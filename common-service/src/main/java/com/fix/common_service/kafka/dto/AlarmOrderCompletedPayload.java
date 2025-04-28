package com.fix.common_service.kafka.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AlarmOrderCompletedPayload {
    private Long userId;
    private UUID gameId;
}
