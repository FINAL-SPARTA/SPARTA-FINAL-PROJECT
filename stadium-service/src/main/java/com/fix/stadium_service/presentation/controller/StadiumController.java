package com.fix.stadium_service.presentation.controller;


import com.fix.common_service.dto.CommonResponse;
import com.fix.stadium_service.application.dtos.request.StadiumCreateRequest;
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
    public ResponseEntity<CommonResponse<StadiumResponseDto>> createStadium(@RequestBody StadiumCreateRequest requestDto){
        StadiumResponseDto responseDto = stadiumService.createStadium(requestDto);
        return ResponseEntity.ok(CommonResponse.success(responseDto, "경기장 생성 성공"));
    }

    //경기장 단건 조회
    @GetMapping("/{stadiumId}")
    public ResponseEntity<CommonResponse<StadiumResponseDto>> getStadium(@PathVariable("stadiumId") UUID stadiumId){
        StadiumResponseDto responseDto = stadiumService.getStadium(stadiumId);
        return ResponseEntity.ok(CommonResponse.success(responseDto,"경기장 상세 조회 성공"));

    }
//    // 경기장 전체 조회
//    @GetMapping
//    public ResponseEntity<CommonResponse<StadiumResponseDto>> getStadiums()



}
