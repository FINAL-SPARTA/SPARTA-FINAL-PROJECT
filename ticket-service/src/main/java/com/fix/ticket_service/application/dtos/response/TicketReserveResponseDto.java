package com.fix.ticket_service.application.dtos.response;

import com.fix.ticket_service.domain.model.Ticket;
import com.fix.ticket_service.domain.model.TicketStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
public class TicketReserveResponseDto {
    private UUID ticketId;
    private Long userId;
    private UUID gameId;
    private UUID seatId;
    private int price;
    private TicketStatus status;

    public TicketReserveResponseDto(Ticket ticket) {
        this.ticketId = ticket.getTicketId();
        this.userId = ticket.getUserId();
        this.gameId = ticket.getGameId();
        this.seatId = ticket.getSeatId();
        this.price = ticket.getPrice();
        this.status = ticket.getStatus();
    }
}
