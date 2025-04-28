package com.fix.event_service.infrastructure.quartz;

import com.fix.event_service.application.service.EventApplicationService;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class EventStatusJob implements Job {

    public static final String KEY_EVENT_ID = "eventId";
    public static final String KEY_ACTION = "action";  // "START" or "END"

    @Autowired
    private EventApplicationService eventApplicationService;

    @Override
    public void execute(JobExecutionContext context) {
        JobDataMap data = context.getMergedJobDataMap();
        UUID eventId = UUID.fromString(data.getString(KEY_EVENT_ID));
        String action = data.getString(KEY_ACTION);

        // 액션에 따라 이벤트 서비스 메서드 호출
        if ("START".equals(action)) {
            eventApplicationService.startEventByScheduler(eventId);
        } else if ("END".equals(action)) {
            eventApplicationService.endEventAndAnnounceByScheduler(eventId);
        }
    }
}
