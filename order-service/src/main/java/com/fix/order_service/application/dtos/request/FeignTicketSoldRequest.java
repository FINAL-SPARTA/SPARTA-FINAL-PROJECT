package com.fix.order_service.application.dtos.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.UUID;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FeignTicketSoldRequest {
    private UUID orderId;
    private List<UUID> ticketIds;
}
//  주문 완료 후, ticket-service에 티켓 상태를 SOLD로 변경 요청