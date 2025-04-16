package com.fix.common_service.kafka.dto;

import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class PointDeductionFailedPayload {
    private UUID eventId;
    private Long userId;
    private UUID eventEntryId;
    private Integer pointsAttempted; // 차감 시도 포인트
    private String failureReason; // 실패 사유
}
