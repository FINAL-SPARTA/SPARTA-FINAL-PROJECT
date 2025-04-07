package com.fix.event_service.presentation.controller;

import com.fix.common_service.dto.CommonResponse;
import com.fix.event_service.application.dtos.request.EventCreateRequestDto;
import com.fix.event_service.application.dtos.response.EventDetailResponseDto;
import com.fix.event_service.application.dtos.response.EventEntryResponseDto;
import com.fix.event_service.application.dtos.response.EventResponseDto;
import com.fix.event_service.application.dtos.response.PageResponseDto;
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

    // ✅ 이벤트 단건 상세 조회 API
    @GetMapping("/{eventId}")
    public ResponseEntity<CommonResponse<EventDetailResponseDto>> getEvent(@PathVariable("eventId") UUID eventId) {
        EventDetailResponseDto responseDto = eventApplicationService.getEvent(eventId);
        return ResponseEntity.ok(CommonResponse.success(responseDto, "이벤트 상세 조회 성공"));
    }

    // ✅ 이벤트 목록 조회
    @GetMapping("")
    public ResponseEntity<CommonResponse<PageResponseDto<EventResponseDto>>> getEvents(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size) {
        PageResponseDto<EventResponseDto> responseDto = eventApplicationService.getEvents(page, size);
        return ResponseEntity.ok(CommonResponse.success(responseDto, "이벤트 목록(페이지) 조회 성공"));
    }

    // ✅ 특정 이벤트 응모 기록(목록) 조회
    @GetMapping("/{eventId}/entries")
    public ResponseEntity<CommonResponse<PageResponseDto<EventEntryResponseDto>>> getEventEntries(
            @PathVariable("eventId") UUID eventId,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size) {
        PageResponseDto<EventEntryResponseDto> responseDto = eventApplicationService.getEventEntries(eventId, page, size);
        return ResponseEntity.ok(CommonResponse.success(responseDto, "이벤트 응모 기록(목록) 조회 성공"));
    }
}
