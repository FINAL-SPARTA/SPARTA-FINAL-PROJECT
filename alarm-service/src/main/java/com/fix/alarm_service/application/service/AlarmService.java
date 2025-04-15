package com.fix.alarm_service.application.service;

import com.fix.alarm_service.application.dtos.request.AligoSmsRequestDto;
import com.fix.alarm_service.application.dtos.response.PhoneNumberResponseDto;
import com.fix.alarm_service.infrastructure.UserClient;
import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
public class AlarmService {

    @Value("${aligo.api-url}")
    private String apiUrl;
    @Value("${aligo.key}")
    private String apiKey;
    @Value("${aligo.user-id}")
    private String userId;
    @Value("${aligo.sender}")
    private String sender;

    private final UserClient userClient;

    public PhoneNumberResponseDto getPhoneNumber(Long userId){
        return userClient.getPhoneNumber(userId);
    }

    public String sendSms(AligoSmsRequestDto requestDto){
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();

        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String,String> params = new LinkedMultiValueMap<>();
        params.add("key",apiKey);
        params.add("user_id",userId);
        params.add("sender",sender);
        params.add("receiver",requestDto.getReceiver());
        params.add("msg",requestDto.getMsg());
        params.add("msg_type","SMS");

        HttpEntity<MultiValueMap<String,String>> entity = new HttpEntity<>(params,headers);
        ResponseEntity<String> response = restTemplate.postForEntity(apiUrl + "/msg" ,entity, String.class );
        return response.getBody();
    }



}
