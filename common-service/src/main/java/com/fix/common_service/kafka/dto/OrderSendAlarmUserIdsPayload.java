package com.fix.common_service.kafka.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class OrderSendAlarmUserIdsPayload {
    private UUID gameId;
    private List<Long> userIds;
}
