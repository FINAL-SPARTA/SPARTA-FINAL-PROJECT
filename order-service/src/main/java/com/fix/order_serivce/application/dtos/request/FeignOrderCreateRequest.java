package com.fix.order_serivce.application.dtos.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class FeignOrderCreateRequest {
    private List<FeignTicketReserveDto> ticketDtoList;

    public FeignOrderCreateRequest(List<FeignTicketReserveDto> ticketDtoList) {
        this.ticketDtoList = ticketDtoList;
    }
}
//  예약된 티켓 목록을 기반으로 주문 생성 요청