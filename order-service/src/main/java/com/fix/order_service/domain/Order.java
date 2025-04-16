package com.fix.order_service.domain;

import com.fix.common_service.entity.Basic;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;


@Entity
@Table(name = "p_order")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Order extends Basic {

    @Id
    @Column(name = "order_id", nullable = false, updatable = false)
    private UUID orderId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "game_id", nullable = false)
    private UUID gameId;

    @Enumerated(EnumType.STRING)
    @Column(name = "order_status", nullable = false)
    private OrderStatus orderStatus;

    @Column(name = "people_count", nullable = false)
    private int peopleCount;

    @Column(name = "total_price", nullable = false)
    private int totalPrice;

    @Builder
    private Order(UUID orderId, Long userId, UUID gameId, OrderStatus orderStatus, int peopleCount, int totalPrice) {
        this.orderId = orderId;
        this.userId = userId;
        this.gameId = gameId;
        this.orderStatus = orderStatus;
        this.peopleCount = peopleCount;
        this.totalPrice = totalPrice;
    }

    public static Order create(Long userId, UUID gameId, OrderStatus orderStatus, int peopleCount, int totalPrice) {
        return Order.builder()
                .orderId(UUID.randomUUID())
                .userId(userId)
                .gameId(gameId)
                .orderStatus(orderStatus)
                .peopleCount(peopleCount)
                .totalPrice(totalPrice)
                .build();
    }
    public void complete() {this.orderStatus = OrderStatus.COMPLETED;}

    public void cancel() {
        this.orderStatus = OrderStatus.CANCELLED;
    }

    public void update(int peopleCount, OrderStatus orderStatus) {
        this.peopleCount = peopleCount;
        this.orderStatus = orderStatus;
    }

}