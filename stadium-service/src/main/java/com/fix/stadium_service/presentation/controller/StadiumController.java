package com.fix.stadium_service.presentation.controller;


import com.fix.common_service.dto.CommonResponse;
import com.fix.stadium_service.application.dtos.request.StadiumCreateRequest;
import com.fix.stadium_service.application.dtos.request.StadiumUpdateRequest;
import com.fix.stadium_service.application.dtos.response.PageResponseDto;
import com.fix.stadium_service.application.dtos.response.StadiumResponseDto;
import com.fix.stadium_service.application.service.StadiumService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RequiredArgsConstructor
@RestController
@RequestMapping("api/v1/stadiums")
public class StadiumController {

    private final StadiumService stadiumService;

    // 경기장 생성
    @PostMapping()
    public ResponseEntity<CommonResponse<StadiumResponseDto>> createStadium(@RequestBody StadiumCreateRequest requestDto) {
        StadiumResponseDto responseDto = stadiumService.createStadium(requestDto);
        return ResponseEntity.ok(CommonResponse.success(responseDto, "경기장 생성 성공"));
    }

    //경기장 단건 조회
    @GetMapping("/{stadiumId}")
    public ResponseEntity<CommonResponse<StadiumResponseDto>> getStadium(@PathVariable("stadiumId") UUID stadiumId) {
        StadiumResponseDto responseDto = stadiumService.getStadium(stadiumId);
        return ResponseEntity.ok(CommonResponse.success(responseDto, "경기장 상세 조회 성공"));

    }


    // 경기장 전체 조회(페이징)
    @GetMapping()
    public ResponseEntity<CommonResponse<PageResponseDto>> getStadiums(@RequestParam(value = "page", defaultValue = "0") int page, @RequestParam(value = "size", defaultValue = "10") int size) {
        PageResponseDto responseDto = stadiumService.getStadiums(page, size);
        return ResponseEntity.ok(CommonResponse.success(responseDto, "경기장 목록(페이지) 조회 성공"));
    }


    @DeleteMapping("/{stadiumId}")
    public ResponseEntity<CommonResponse<Void>> deleteStadium(@PathVariable("stadiumId") UUID stadiumId) {
        stadiumService.deleteStadium(stadiumId);
        return ResponseEntity.ok(CommonResponse.success(null, "경기장 삭제 성공"));

    }

    @PatchMapping("/{stadiumId}")
    public ResponseEntity<CommonResponse<StadiumResponseDto>> updateStadium(
            @PathVariable UUID stadiumId,
            @RequestBody  StadiumUpdateRequest request) {
        StadiumResponseDto response = stadiumService.updateStadium(stadiumId, request);
        return ResponseEntity.ok(CommonResponse.success(response, "경기장 수정 완료"));
    }








}
