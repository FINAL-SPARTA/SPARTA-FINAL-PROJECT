package com.fix.alarm_service.application.service;

import com.fix.alarm_service.application.dtos.response.PhoneNumberResponseDto;
import com.fix.alarm_service.domain.model.GameAlarmSchedule;
import com.fix.alarm_service.domain.repository.GameAlarmScheduleRepository;
import com.fix.alarm_service.infrastructure.UserClient;
import com.fix.alarm_service.infrastructure.kafka.producer.AlarmProducer;
import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.PublishRequest;
import software.amazon.awssdk.services.sns.model.PublishResponse;
import software.amazon.awssdk.services.sns.model.SnsException;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AlarmService {

    private final GameAlarmScheduleRepository scheduleRepository;
    private final AlarmProducer alarmProducer;
    private final UserClient userClient;
    private final SnsClient snsClient;




    @Scheduled(cron = "0/30 * * * * *") //매일 00:00:00 에 한번 실행
    public void publishGamesAlarmStartingTomorrow(){

        // 현재 시간 기준
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime from = now.plusDays(1).withHour(0).withMinute(0).withSecond(0);
        LocalDateTime to = now.plusDays(1).withHour(23).withMinute(59).withSecond(59);
        List<GameAlarmSchedule> schedules = scheduleRepository.findByGameDateBetweenAndIsSentFalse(from, to);

        if(schedules.isEmpty()){
            log.info("[Scheduler] 내일 경기 없음. 이벤트 발행 스킵");
            return;
        }

        for(GameAlarmSchedule schedule : schedules){
            alarmProducer.sendGameIdToOrder(schedule.getGameId());
            schedule.markAsSent(); // 발행했으니 sent = true
            log.info("[scheduler] 경기 알림 발행 완료 - gameId:{}",schedule.getGameId());

        }
        scheduleRepository.saveAll(schedules);
    }






    public PhoneNumberResponseDto getPhoneNumber(Long userId){
        return userClient.getPhoneNumber(userId);
    }



    public String sendSns(String rawPhoneNumber, String message){

        try{
            String formattedPhoneNumber = formatPhoneNumber(rawPhoneNumber);

            PublishRequest request = PublishRequest.builder()
                    .message(message)
                    .phoneNumber(formattedPhoneNumber)
                    .build();

            PublishResponse response = snsClient.publish(request);

            log.info("SNS 발송 성공: {}", response.messageId());
            return response.messageId();
        } catch (SnsException e){
            log.error("SNS 발송 실패 : {}", e.awsErrorDetails().errorMessage());
            throw new RuntimeException("SNS 발송 중 오류가 발생했습니다.");

        }
    }

    @Transactional
    public void saveSchedule(GameAlarmSchedule schedule){
        scheduleRepository.save(schedule);
    }




    private String formatPhoneNumber(String rawPhoneNumber){
        if(rawPhoneNumber == null || rawPhoneNumber.isBlank()){
            throw new IllegalArgumentException("전화번호가 유효하지 않습니다") ;//TODO 공통 예외처리하기

        }
        return rawPhoneNumber.startsWith("0")
                ? "+82" +rawPhoneNumber.substring(1)
                : rawPhoneNumber;

    }




}
