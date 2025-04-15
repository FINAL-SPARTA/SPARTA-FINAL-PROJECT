package com.fix.common_service.dto;

import lombok.*;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TicketReservedPayload {
    private List<TicketDetail> ticketDetails;
    private Long userId;
    private UUID gameId;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TicketDetail {
        private UUID ticketId;
        private int price;
    }
}
