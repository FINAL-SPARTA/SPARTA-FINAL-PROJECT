package com.fix.ticket_service.application.dtos.response;

import com.fix.ticket_service.domain.model.Ticket;
import com.fix.ticket_service.domain.model.TicketStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@NoArgsConstructor
public class TicketDetailResponseDto {
    private UUID ticketId;
    private Long userId;
    private UUID gameId;
    private UUID seatId;
    private int price;
    private TicketStatus status;
    private String soldAt;
    private UUID orderId;

    public TicketDetailResponseDto(Ticket ticket) {
        this.ticketId = ticket.getTicketId();
        this.userId = ticket.getUserId();
        this.gameId = ticket.getGameId();
        this.seatId = ticket.getSeatId();
        this.price = ticket.getPrice();
        this.status = ticket.getStatus();
        this.soldAt = ticket.getSoldAt() != null ? ticket.getSoldAt().toString() : null;
        this.orderId = ticket.getOrderId();
    }
}
