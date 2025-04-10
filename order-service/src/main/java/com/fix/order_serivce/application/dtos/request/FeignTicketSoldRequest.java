package com.fix.order_serivce.application.dtos.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;


@Getter
@NoArgsConstructor
@AllArgsConstructor
public class FeignTicketSoldRequest {
    private UUID orderId;
    private List<UUID> ticketIds;
}
//  주문 완료 후, ticket-service에 티켓 상태를 SOLD로 변경 요청