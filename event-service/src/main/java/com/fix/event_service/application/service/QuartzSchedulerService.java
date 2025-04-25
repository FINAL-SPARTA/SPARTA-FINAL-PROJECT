package com.fix.event_service.application.service;

import com.fix.event_service.infrastructure.quartz.EventStatusJob;
import lombok.RequiredArgsConstructor;
import org.quartz.*;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class QuartzSchedulerService {

    private final Scheduler scheduler;

    public void scheduleEventJob(UUID eventId, Date fireAt, String action) {
        // JobName: "eventId-action"
        String jobName = eventId + "-" + action;
        JobDetail jobDetail = JobBuilder.newJob(EventStatusJob.class)
            .withIdentity(jobName, "event-jobs")
            // JobDataMap에 파라미터 저장
            .usingJobData(EventStatusJob.KEY_EVENT_ID, eventId.toString())
            .usingJobData(EventStatusJob.KEY_ACTION, action)
            .build();

        Trigger trigger = TriggerBuilder.newTrigger()
            .withIdentity(jobName + "-trigger", "event-triggers")
            .startAt(fireAt)  // 지정된 시각에 한 번만 실행
            .withSchedule(SimpleScheduleBuilder.simpleSchedule()
                .withMisfireHandlingInstructionFireNow())
            .build();

        try {
            // 이미 등록된 Job 이 있으면 삭제 후 재등록
            if (scheduler.checkExists(jobDetail.getKey())) {
                scheduler.deleteJob(jobDetail.getKey());
            }
            scheduler.scheduleJob(jobDetail, trigger);
        } catch (SchedulerException e) {
            throw new RuntimeException("Quartz 스케쥴 등록 실패", e);
        }
    }

    public void removeEventJobs(UUID eventId) {
        try {
            scheduler.deleteJob(new JobKey(eventId + "-START", "event-jobs"));
            scheduler.deleteJob(new JobKey(eventId + "-END", "event-jobs"));
        } catch (SchedulerException e) {
            throw new RuntimeException("Quartz 스케쥴 삭제 실패", e);
        }
    }
}
