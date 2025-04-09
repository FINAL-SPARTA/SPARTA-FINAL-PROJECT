package com.fix.ticket_service.application.dtos.response;

import com.fix.ticket_service.domain.model.Ticket;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@NoArgsConstructor
public class TicketResponseDto {
    private UUID ticketId;
    private Long userId;
    private UUID gameId;
    private UUID seatId;

    public TicketResponseDto(Ticket ticket) {
        this.ticketId = ticket.getTicketId();
        this.userId = ticket.getUserId();
        this.gameId = ticket.getGameId();
        this.seatId = ticket.getSeatId();
    }
}
