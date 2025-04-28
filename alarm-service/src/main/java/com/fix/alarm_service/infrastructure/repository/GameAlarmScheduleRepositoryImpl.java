package com.fix.alarm_service.infrastructure.repository;

import com.fix.alarm_service.domain.model.GameAlarmSchedule;
import com.fix.alarm_service.domain.repository.GameAlarmScheduleRepository;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class GameAlarmScheduleRepositoryImpl implements GameAlarmScheduleRepository {

    private final JpaGameAlarmScheduleRepository  jpaGameAlarmScheduleRepository;





    @Override
    public void save(GameAlarmSchedule gameAlarmSchedule) {jpaGameAlarmScheduleRepository.save(gameAlarmSchedule);
    }

    @Override
    public List<GameAlarmSchedule> findByGameDateBetweenAndIsSentFalse(LocalDateTime from, LocalDateTime to) {
        return jpaGameAlarmScheduleRepository.findByGameDateBetweenAndSentFalse(from,to);

    }

    @Override
    public List<GameAlarmSchedule> saveAll(Iterable<GameAlarmSchedule> schedules) {
        return jpaGameAlarmScheduleRepository.saveAll(schedules);

    }
}
