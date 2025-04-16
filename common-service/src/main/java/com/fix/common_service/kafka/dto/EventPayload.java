package com.fix.common_service.kafka.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EventPayload {
    private UUID eventId;      // 이벤트 식별자
    private Long userId;       // 유저 식별자
    private Integer points;    // 차감할 포인트
}
