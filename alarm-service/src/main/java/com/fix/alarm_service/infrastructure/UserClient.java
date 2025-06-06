package com.fix.alarm_service.infrastructure;

import com.fix.alarm_service.application.dtos.response.PhoneNumberResponseDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name ="user-service")
public interface UserClient {
    @GetMapping("api/v1/users/feign/{userId}/phone-number")
    PhoneNumberResponseDto getPhoneNumber(@PathVariable("userId")Long userId);
}
