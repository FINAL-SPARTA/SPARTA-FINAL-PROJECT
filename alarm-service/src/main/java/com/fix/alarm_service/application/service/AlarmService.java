package com.fix.alarm_service.application.service;

import com.fix.alarm_service.application.dtos.response.PhoneNumberResponseDto;
import com.fix.alarm_service.infrastructure.UserClient;
import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.PublishRequest;
import software.amazon.awssdk.services.sns.model.PublishResponse;
import software.amazon.awssdk.services.sns.model.SnsException;

@Slf4j
@Service
@RequiredArgsConstructor
public class AlarmService {

    private final UserClient userClient;
    private final SnsClient snsClient;


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


    private String formatPhoneNumber(String rawPhoneNumber){
        if(rawPhoneNumber == null || rawPhoneNumber.isBlank()){
            throw new IllegalArgumentException("전화번호가 유효하지 않습니다") ;//TODO 공통 예외처리하기

        }
        return rawPhoneNumber.startsWith("0")
                ? "+82" +rawPhoneNumber.substring(1)
                : rawPhoneNumber;

    }




}
