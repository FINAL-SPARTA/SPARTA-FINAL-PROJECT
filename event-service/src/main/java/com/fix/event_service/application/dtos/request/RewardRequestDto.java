package com.fix.event_service.application.dtos.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class RewardRequestDto {
    private String rewardName;
    private Integer quantity;
    private String description;
}
