package com.fix.alarm_service.domain.repository;


import com.fix.alarm_service.domain.model.GameAlarmSchedule;

import java.time.LocalDateTime;
import java.util.List;

public interface GameAlarmScheduleRepository {

    void save(GameAlarmSchedule gameAlarmSchedule);
    List<GameAlarmSchedule> findByGameDateBetweenAndIsSentFalse(LocalDateTime from, LocalDateTime to);
    List<GameAlarmSchedule> saveAll(Iterable<GameAlarmSchedule> schedules);

}
