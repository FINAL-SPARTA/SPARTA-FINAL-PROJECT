package com.fix.stadium_service.presentation.controller;


import com.fix.common_service.dto.CommonResponse;
import com.fix.stadium_service.application.dtos.request.StadiumCreateRequest;
import com.fix.stadium_service.application.dtos.response.StadiumResponseDto;
import com.fix.stadium_service.application.service.StadiumService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("api/v1/stadiums")
public class StadiumController {

    private final StadiumService stadiumService;


    @PostMapping()
    public ResponseEntity<CommonResponse<StadiumResponseDto>> createStadium(@RequestBody StadiumCreateRequest requestDto){
        StadiumResponseDto responseDto = stadiumService.createStadium(requestDto);
        return ResponseEntity.ok(CommonResponse.success(responseDto, "경기장 생성 성공"));
    }

}
