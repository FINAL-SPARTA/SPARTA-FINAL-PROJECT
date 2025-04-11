package com.fix.stadium_service.domain.model;

import java.util.Arrays;

public enum StadiumName {
    KIA_DIA("기아챔피언스필드", 20500),
    SAM_DIA("삼성라이온즈파크", 24000),
    LG_DIA("서울종합운동장(LG)", 23700),
    SSG_DIA("SSG랜더스필드", 23000),
    KT_DIA("수원KT위즈파크", 20000),
    HAN_DIA("한화생명불파크(신)", 22000);

    private final String displayName;
    private final int seatCapacity;

    StadiumName(String displayName, int seatCapacity) {
        this.displayName = displayName;
        this.seatCapacity = seatCapacity;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getSeatCapacity() {
        return seatCapacity;
    }

    public static StadiumName fromTeamName(String teamName) {
        return Arrays.stream(values())
                .filter(s -> s.name().contains(teamName.toUpperCase()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("해당 팀 이름에 대한 경기장을 찾을 수 없습니다."));
    }

}