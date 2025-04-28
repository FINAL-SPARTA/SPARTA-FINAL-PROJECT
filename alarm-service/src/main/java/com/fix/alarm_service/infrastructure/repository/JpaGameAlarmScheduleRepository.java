package com.fix.alarm_service.infrastructure.repository;

import com.fix.alarm_service.domain.model.GameAlarmSchedule;
import com.fix.alarm_service.domain.repository.GameAlarmScheduleRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface JpaGameAlarmScheduleRepository extends JpaRepository<GameAlarmSchedule, UUID> {

  List<GameAlarmSchedule> findByGameDateBetweenAndSentFalse(LocalDateTime from, LocalDateTime to);
}


