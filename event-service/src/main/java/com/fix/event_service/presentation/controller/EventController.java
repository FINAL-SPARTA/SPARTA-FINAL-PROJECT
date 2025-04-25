package com.fix.event_service.presentation.controller;

import com.fix.common_service.aop.ApiLogging;
import com.fix.common_service.dto.CommonResponse;
import com.fix.event_service.application.aop.ValidateUser;
import com.fix.event_service.application.dtos.request.EventCreateRequestDto;
import com.fix.event_service.application.dtos.request.EventUpdateRequestDto;
import com.fix.event_service.application.dtos.response.*;
import com.fix.event_service.application.service.EventApplicationService;
import com.fix.event_service.domain.model.EventStatus;
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
    @ValidateUser(roles = {"MASTER", "MANAGER"})
    @PostMapping("")
    public ResponseEntity<CommonResponse<EventDetailResponseDto>> createEvent(@RequestBody EventCreateRequestDto requestDto) {
        EventDetailResponseDto responseDto = eventApplicationService.createEvent(requestDto);
        return ResponseEntity.ok(CommonResponse.success(responseDto, "이벤트 생성 성공"));
    }

    // ✅ 이벤트 응모 API
    @ApiLogging
    @PostMapping("/{eventId}")
    public ResponseEntity<CommonResponse<Void>> applyEvent(
        @PathVariable("eventId") UUID eventId, @RequestHeader("x-user-id") Long userId) {
        eventApplicationService.applyEvent(eventId, userId);
        return ResponseEntity.ok(CommonResponse.success(null, "이벤트 응모 신청 성공"));
    }

    // ✅ 이벤트 단건 상세 조회 API
    @GetMapping("/{eventId}")
    public ResponseEntity<CommonResponse<EventDetailResponseDto>> getEvent(@PathVariable("eventId") UUID eventId) {
        EventDetailResponseDto responseDto = eventApplicationService.getEvent(eventId);
        return ResponseEntity.ok(CommonResponse.success(responseDto, "이벤트 상세 조회 성공"));
    }

    // ✅ 이벤트 목록 조회 API
    @GetMapping("")
    public ResponseEntity<CommonResponse<PageResponseDto<EventResponseDto>>> getEvents(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size) {
        PageResponseDto<EventResponseDto> responseDto = eventApplicationService.getEvents(page, size);
        return ResponseEntity.ok(CommonResponse.success(responseDto, "이벤트 목록(페이지) 조회 성공"));
    }

    // ✅ 특정 이벤트 응모 기록(목록) 조회 API
    @ValidateUser(roles = {"MASTER", "MANAGER"})
    @GetMapping("/{eventId}/entries")
    public ResponseEntity<CommonResponse<PageResponseDto<EventEntryResponseDto>>> getEventEntries(
            @PathVariable("eventId") UUID eventId,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size) {
        PageResponseDto<EventEntryResponseDto> responseDto = eventApplicationService.getEventEntries(eventId, page, size);
        return ResponseEntity.ok(CommonResponse.success(responseDto, "이벤트 응모 기록(목록) 조회 성공"));
    }

    // ✅ 이벤트 검색 API
    @ApiLogging
    @GetMapping("/search")
    public ResponseEntity<CommonResponse<PageResponseDto<EventResponseDto>>> searchEvents(
            @RequestParam("status") EventStatus status,
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size) {
        PageResponseDto<EventResponseDto> responseDto = eventApplicationService.searchEvents(status, keyword, page, size);
        return ResponseEntity.ok(CommonResponse.success(responseDto, "이벤트 검색 성공"));
    }

    // ✅ 이벤트 정보 수정 API
    @ValidateUser(roles = {"MASTER", "MANAGER"})
    @PutMapping("/{eventId}")
    public ResponseEntity<CommonResponse<EventDetailResponseDto>> updateEvent(
            @PathVariable("eventId") UUID eventId,
            @RequestBody EventUpdateRequestDto requestDto) {
        EventDetailResponseDto responseDto = eventApplicationService.updateEvent(eventId, requestDto);
        return ResponseEntity.ok(CommonResponse.success(responseDto, "이벤트 정보 수정 성공"));
    }

    // ✅ 이벤트 논리적 삭제 API
    @ValidateUser(roles = {"MASTER", "MANAGER"})
    @DeleteMapping("/{eventId}")
    public ResponseEntity<CommonResponse<Void>> deleteEvent(
        @PathVariable("eventId") UUID eventId, @RequestHeader("x-user-id") Long userId) {
        eventApplicationService.deleteEvent(eventId, userId);
        return ResponseEntity.ok(CommonResponse.success(null, "이벤트 삭제 성공"));
    }
}
