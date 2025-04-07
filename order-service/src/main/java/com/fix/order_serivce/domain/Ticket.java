package com.fix.order_serivce.domain;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "p_ticket")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Ticket {

    @Id
    @Column(name = "ticket_id", nullable = false, updatable = false)
    private UUID ticketId;

    @Column(name = "order_id", nullable = false)
    private UUID orderId;

    @Column(name = "seat_id", nullable = false)
    private UUID seatId;

    @Column(name = "price", nullable = false)
    private int price;

    @Builder
    private Ticket(UUID ticketId, UUID orderId, UUID seatId, int price) {
        this.ticketId = ticketId;
        this.orderId = orderId;
        this.seatId = seatId;
        this.price = price;
    }

    public static Ticket create(UUID orderId, UUID seatId, int price) {
        return Ticket.builder()
                .ticketId(UUID.randomUUID())
                .orderId(orderId)
                .seatId(seatId)
                .price(price)
                .build();
    }
}
