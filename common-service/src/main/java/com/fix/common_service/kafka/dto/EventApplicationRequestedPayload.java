package com.fix.common_service.kafka.dto;

import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class EventApplicationRequestedPayload {
    private UUID eventId;      // 이벤트 식별자
    private Long userId;       // 유저 식별자
    private Integer points;    // 차감할 포인트
    private UUID eventEntryId; // 이벤트 응모 식별자 (보상 트랜잭션 시 식별 용)
}
