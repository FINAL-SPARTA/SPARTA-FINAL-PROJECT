package com.fix.event_service.application.dtos.response;

import com.fix.event_service.domain.model.Reward;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@NoArgsConstructor
public class RewardResponseDto {
    private UUID rewardId;
    private String rewardName;
    private Integer quantity;
    private String description;

    public RewardResponseDto(Reward reward) {
        this.rewardId = reward.getRewardId();
        this.rewardName = reward.getRewardName();
        this.quantity = reward.getQuantity();
        this.description = reward.getDescription();
    }
}
