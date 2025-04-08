package com.fix.stadium_service.domain.model;

public enum SeatSection {

    VIP("VIP석", 150000),
    R_SECTION("R석", 120000),
    S_SECTION("S석", 90000),
    A_SECTION("A석", 60000),
    B_SECTiON("B석",50000),
    OUTFIELD("OutField",40000);

    private final String displayName;
    private final int price;

    SeatSection(String displayName, int price) {
        this.displayName = displayName;
        this.price = price;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getPrice() {
        return price;
    }
}
