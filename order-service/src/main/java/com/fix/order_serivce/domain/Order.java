package com.fix.order_serivce.domain;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "p_order")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Order {

    @Id
    @Column(name = "order_id", nullable = false, updatable = false)
    private UUID orderId;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "game_id", nullable = false)
    private UUID gameId;

    @Enumerated(EnumType.STRING)
    @Column(name = "order_status", nullable = false)
    private OrderStatus orderStatus;

    @Column(name = "people_count", nullable = false)
    private int peopleCount;

    @Column(name = "total_count", nullable = false)
    private int totalCount;

    @Builder
    private Order(UUID orderId, UUID userId, UUID gameId, OrderStatus orderStatus, int peopleCount, int totalCount) {
        this.orderId = orderId;
        this.userId = userId;
        this.gameId = gameId;
        this.orderStatus = orderStatus;
        this.peopleCount = peopleCount;
        this.totalCount = totalCount;
    }

    public static Order create(UUID userId, UUID gameId, OrderStatus orderStatus, int peopleCount, int totalCount) {
        return Order.builder()
                .orderId(UUID.randomUUID())
                .userId(userId)
                .gameId(gameId)
                .orderStatus(orderStatus)
                .peopleCount(peopleCount)
                .totalCount(totalCount)
                .build();
    }

    public void cancel() {
        this.orderStatus = OrderStatus.CANCELLED;
    }
}