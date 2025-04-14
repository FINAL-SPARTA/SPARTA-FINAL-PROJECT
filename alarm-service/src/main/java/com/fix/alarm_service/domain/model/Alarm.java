package com.fix.alarm_service.domain.model;

import com.fix.common_service.entity.Basic;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;


@Entity
@Table(name="p_alarm")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Alarm extends Basic {

    @Id
    @GeneratedValue(generator = "UUID")
    @Column(name = "alarm_id",updatable = false,nullable = false)
    private UUID alarmId;

    private Long userId;

    @Enumerated(EnumType.STRING)
    private AlarmType type;

    private String title;

    private String content;

     //private boolean isRead;   나중에 user가 요청하면..

    private LocalDateTime scheduleAt; // 예약 발송 시간 (null -> 즉시발송)

    private LocalDateTime sentAt; // 실제 발송 시간

}
