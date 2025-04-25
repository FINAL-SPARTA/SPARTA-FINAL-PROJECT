package com.fix.alarm_service.domain.repository;


import com.fix.alarm_service.domain.model.GameAlarmSchedule;

public interface GameAlarmScheduleRepository {

    GameAlarmSchedule save(GameAlarmSchedule gameAlarmSchedule);
}
