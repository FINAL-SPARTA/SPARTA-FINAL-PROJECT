package com.fix.ticket_service.domain.model;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "p_ticket")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Ticket {

    @Id
    @Column(name = "ticket_id", nullable = false, updatable = false)
    private UUID ticketId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "game_id", nullable = false)
    private UUID gameId;

    @Column(name = "seat_id", nullable = false)
    private UUID seatId;

    @Column(name = "price", nullable = false)
    private int price;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private TicketStatus status;

    @Column(name = "sold_at")
    private LocalDateTime soldAt;

    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;

    @Column(name = "order_id")
    private UUID orderId;

    @Builder
    private Ticket(Long userId, UUID gameId, UUID seatId, int price) {
        this.ticketId = UUID.randomUUID();
        this.userId = userId;
        this.gameId = gameId;
        this.seatId = seatId;
        this.price = price;
        this.status = TicketStatus.RESERVED;
    }

    public static Ticket create(Long userId, UUID gameId, UUID seatId, int price) {
        return Ticket.builder()
                .userId(userId)
                .gameId(gameId)
                .seatId(seatId)
                .price(price)
                .build();
    }

    public void markAsSold(UUID orderId) {
        this.status = TicketStatus.SOLD;
        this.orderId = orderId;
        this.soldAt = LocalDateTime.now();
    }

    public void markAsCancelled() {
        this.status = TicketStatus.CANCELLED;
        this.cancelledAt = LocalDateTime.now();
    }

    public void validateAuth(Long userId, String userRole) {
        if (!this.userId.equals(userId) && !(userRole.equals("MASTER") || userRole.equals("MANAGER"))) {
            throw new IllegalArgumentException("조회, 수정 권한이 없습니다.");
        }
    }
}
