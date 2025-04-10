package com.fix.event_service.infrastructure.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "user-service")
public interface UserClient {

    @PostMapping("/api/v1/users/feign/deduct-points/{userId}")
    void deductPoints(@PathVariable("userId") Long userId, @RequestParam("points") Integer requiredPoints);
}
