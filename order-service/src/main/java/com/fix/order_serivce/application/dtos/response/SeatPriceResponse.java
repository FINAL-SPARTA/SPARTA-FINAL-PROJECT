package com.fix.order_serivce.application.dtos.response;


import lombok.Getter;

import java.util.UUID;

@Getter
public class SeatPriceResponse {
    private UUID seatId;
    private String seatSection;
    private int price;
}
