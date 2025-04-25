package com.fix.alarm_service.domain.model;


import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class GameAlarmSchedule {

    @Id
    @GeneratedValue(generator = "UUID")
    private UUID gameAlarmScheduleID;

    @Column(nullable = false,unique = true)
    private UUID gameId;

    @Column(nullable = false)
    private LocalDateTime gameDate;

    private boolean isSent;


    public static GameAlarmSchedule of (UUID gameId, LocalDateTime gameDate){
        return GameAlarmSchedule.builder()
                .gameId(gameId)
                .gameDate(gameDate)
                .isSent(false)
                .build();
    }



}
