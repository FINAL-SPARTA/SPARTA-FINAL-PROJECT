package com.fix.order_serivce.application.dtos.request;

import com.fix.order_serivce.domain.TicketStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
public class FeignTicketReserveDto {

    private UUID ticketId;
    private Long userId;
    private UUID gameId;
    private UUID seatId;
    private int price;
    private TicketStatus status;

    public FeignTicketReserveDto(UUID ticketId, Long userId, UUID gameId, UUID seatId, int price, TicketStatus status) {
        this.ticketId = ticketId;
        this.userId = userId;
        this.gameId = gameId;
        this.seatId = seatId;
        this.price = price;
        this.status = status;
    }
}
//  티켓 하나에 대한 예약 정보, order-service가 주문 생성 시 참고

//✔ 주문 생성 시 필요한 정보가 포함됨	userId, gameId, seatId, price 등
//✔ ticket-service가 보내주는 예약 티켓 정보 스냅샷	order 입장에서 "이 예약으로 주문을 만들면 돼"라는 지시를 받는 셈
//✔ 주문 저장 및 Kafka 이벤트 발행 시 활용됨	좌석 수, 가격, 주문자 정보 계산에 사용