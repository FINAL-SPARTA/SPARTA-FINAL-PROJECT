package com.fix.ticket_service.application.dtos.request;

import com.fix.ticket_service.application.dtos.response.TicketReserveResponseDto;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class OrderCreateRequestDto {
    private List<TicketReserveResponseDto> ticketDtoList;

    public OrderCreateRequestDto(List<TicketReserveResponseDto> ticketDtoList) {
        this.ticketDtoList = ticketDtoList;
    }
}
