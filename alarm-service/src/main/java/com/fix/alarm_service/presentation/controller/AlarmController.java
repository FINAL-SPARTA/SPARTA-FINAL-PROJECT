package com.fix.alarm_service.presentation.controller;

import com.fix.alarm_service.application.service.AlarmService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/alarms")
public class AlarmController {
    private final AlarmService alarmService;


    @PostMapping
    public ResponseEntity<String> sendSns(@RequestParam Long userId, @RequestParam String msg){
        String phoneNumber = alarmService.getPhoneNumber(userId).getPhoneNumber();
        String result = alarmService.sendSns(phoneNumber,msg);
        return ResponseEntity.ok("메세지 ID:" + result);
    }

}
