package com.fix.event_service.domain.model;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@NoArgsConstructor
@Getter
@Entity
@Table(name = "p_reward")
public class Reward {

    @Id
    private UUID rewardId;

    private String rewardName;
    private Integer quantity;
    private String description;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id")
    private Event event;

    @Builder
    public Reward(String rewardName, Integer quantity, String description) {
        this.rewardId = UUID.randomUUID();
        this.rewardName = rewardName;
        this.quantity = quantity;
        this.description = description;
    }

    public static Reward createReward(String rewardName, Integer quantity, String description) {
        return Reward.builder()
                .rewardName(rewardName)
                .quantity(quantity)
                .description(description)
                .build();
    }

    public void setEvent(Event event) {
        this.event = event;
    }

    public void decreaseQuantity(int amount) {
        if (this.quantity < amount) {
            throw new IllegalStateException("상품 재고가 부족합니다");
        }
        this.quantity -= amount;
    }
}
