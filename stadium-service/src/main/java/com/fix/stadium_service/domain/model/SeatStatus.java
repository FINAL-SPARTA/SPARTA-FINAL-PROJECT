package com.fix.stadium_service.domain.model;

public enum SeatStatus {
    AVAILABLE("예매 가능한 상태"),
    SOLD("예매가 완료된 상태");

    private final String description;

    SeatStatus(String description){
        this.description = description;
    }

    public String getDescription(){
        return description;
    }
}
