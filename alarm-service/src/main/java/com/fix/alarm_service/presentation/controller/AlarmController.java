package com.fix.alarm_service.presentation.controller;

import com.fix.alarm_service.application.dtos.request.AligoSmsRequestDto;
import com.fix.alarm_service.application.dtos.response.PhoneNumberResponseDto;
import com.fix.alarm_service.application.service.AlarmService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/alarms")
public class AlarmController {
    private final AlarmService alarmService;

    @GetMapping("/{userId}")
    public PhoneNumberResponseDto getPhoneNumber(@PathVariable("userId") Long userId) {
        return alarmService.getPhoneNumber(userId);
    }

    @PostMapping("/send")
    public String sendSms(@RequestBody AligoSmsRequestDto requestDto){
        return alarmService.sendSms(requestDto);
    }


}
