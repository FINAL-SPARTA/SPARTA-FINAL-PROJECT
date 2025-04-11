package com.fix.ticket_service.application.dtos.request;

import com.fix.ticket_service.application.dtos.response.TicketReserveResponseDto;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class OrderCreateRequestDto {
    private List<TicketReserveResponseDto> ticketDtoList;

    public OrderCreateRequestDto(List<TicketReserveResponseDto> ticketDtoList) {
        this.ticketDtoList = ticketDtoList;
    }
}
