package com.fix.alarm_service.infrastructure.repository;

import com.fix.alarm_service.domain.model.GameAlarmSchedule;
import com.fix.alarm_service.domain.repository.GameAlarmScheduleRepository;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface JpaGameAlarmScheduleRepository extends JpaRepository<GameAlarmSchedule, UUID>, GameAlarmScheduleRepository {
}
