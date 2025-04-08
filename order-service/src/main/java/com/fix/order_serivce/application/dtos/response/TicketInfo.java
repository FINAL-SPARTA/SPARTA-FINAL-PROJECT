package com.fix.order_serivce.application.dtos.response;

import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
public class TicketInfo {
    private UUID ticketId;
    private UUID seatId;
    private int price;
}