package com.fix.stadium_service.presentation.controller;

import com.fix.stadium_service.application.dtos.response.*;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fix.common_service.dto.StadiumFeignResponse;
import com.fix.common_service.dto.CommonResponse;
import com.fix.stadium_service.application.aop.ValidateUser;
import com.fix.stadium_service.application.dtos.request.StadiumCreateRequest;
import com.fix.stadium_service.application.dtos.request.StadiumUpdateRequest;
import com.fix.stadium_service.application.service.StadiumService;
import com.fix.stadium_service.domain.model.StadiumName;

import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
@RestController
@RequestMapping("api/v1/stadiums")
public class StadiumController {

    private final StadiumService stadiumService;

    // 경기장 생성
    @ValidateUser(roles = {"MASTER"})
    @PostMapping()
    public ResponseEntity<CommonResponse<StadiumResponseDto>> createStadium(@RequestBody StadiumCreateRequest requestDto) {
        StadiumResponseDto responseDto = stadiumService.createStadium(requestDto);
        return ResponseEntity.ok(CommonResponse.success(responseDto, "경기장 생성 성공"));
    }

    //경기장 단건 조회
    @GetMapping("/{stadiumId}")
    public ResponseEntity<CommonResponse<StadiumResponseDto>> getStadium(@PathVariable("stadiumId") Long stadiumId) {
        StadiumResponseDto responseDto = stadiumService.getStadium(stadiumId);
        return ResponseEntity.ok(CommonResponse.success(responseDto, "경기장 상세 조회 성공"));

    }


    // 경기장 전체 조회(페이징)
    @GetMapping()
    public ResponseEntity<CommonResponse<PageResponseDto>> getStadiums(@RequestParam(value = "page", defaultValue = "0") int page, @RequestParam(value = "size", defaultValue = "10") int size) {
        PageResponseDto responseDto = stadiumService.getStadiums(page, size);
        return ResponseEntity.ok(CommonResponse.success(responseDto, "경기장 목록(페이지) 조회 성공"));
    }

    @ValidateUser(roles = {"MASTER"})
    @DeleteMapping("/{stadiumId}")
    public ResponseEntity<CommonResponse<Void>> deleteStadium(@PathVariable("stadiumId") Long stadiumId,
                                                              @RequestHeader("x-user-id") Long userId) {
        stadiumService.deleteStadium(stadiumId, userId);
        return ResponseEntity.ok(CommonResponse.success(null, "경기장 삭제 성공"));

    }

    @ValidateUser(roles = {"MASTER", "MANAGER"})
    @PatchMapping("/{stadiumId}")
    public ResponseEntity<CommonResponse<StadiumResponseDto>> updateStadium(
            @PathVariable Long stadiumId,
            @RequestBody StadiumUpdateRequest request) {
        StadiumResponseDto response = stadiumService.updateStadium(stadiumId, request);
        return ResponseEntity.ok(CommonResponse.success(response, "경기장 수정 완료"));
    }


    @GetMapping("/search")
    public ResponseEntity<CommonResponse<PageResponseDto>> searchStadiums(@RequestParam("name") StadiumName stadiumName,
                                                                          @RequestParam(value = "page", defaultValue = "0") int page,
                                                                          @RequestParam(value = "size", defaultValue = "10") int size) {
        PageResponseDto response = stadiumService.searchStadiums(stadiumName, page, size);
        return ResponseEntity.ok(CommonResponse.success(response, "구장명으로 경기장 검색 성공"));


    }


    @GetMapping("/sections")
    public ResponseEntity<SeatSectionListResponseDto> getSections() {
        return ResponseEntity.ok(stadiumService.getSeatSections());
    }


    @GetMapping("/{home-team}/games")
    public ResponseEntity<StadiumFeignResponse> getStadiumInfo(@PathVariable(name = "home-team") String homeTeam) {
        return ResponseEntity.ok(stadiumService.getStadiumInfoByName(homeTeam));
    }


    @GetMapping("/feign/{stadiumId}/get-seats-by-section")
    SeatInfoListResponseDto getSeatsBySection(@PathVariable("stadiumId") Long stadiumId,
                                              @RequestParam("section") String section) {
        return stadiumService.getSeatBySection(stadiumId, section);
    }


}
