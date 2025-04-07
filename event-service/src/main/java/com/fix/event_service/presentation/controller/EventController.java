package com.fix.event_service.presentation.controller;

import com.fix.common_service.dto.CommonResponse;
import com.fix.event_service.application.dtos.request.EventCreateRequestDto;
import com.fix.event_service.application.dtos.response.EventDetailResponseDto;
import com.fix.event_service.application.service.EventApplicationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/events")
public class EventController {

    private final EventApplicationService eventApplicationService;

    // ✅ 이벤트 생성 API
    @PostMapping("")
    public ResponseEntity<CommonResponse<EventDetailResponseDto>> createEvent(@RequestBody EventCreateRequestDto requestDto) {
        EventDetailResponseDto responseDto = eventApplicationService.createEvent(requestDto);
        return ResponseEntity.ok(CommonResponse.success(responseDto, "이벤트 생성 성공"));
    }

    // ✅ 이벤트 응모 API
    @PostMapping("/{eventId}")
    public ResponseEntity<CommonResponse<Void>> applyEvent(@PathVariable("eventId") UUID eventId) {
        eventApplicationService.applyEvent(eventId);
        return ResponseEntity.ok(CommonResponse.success(null, "이벤트 응모 성공"));
    }
}
