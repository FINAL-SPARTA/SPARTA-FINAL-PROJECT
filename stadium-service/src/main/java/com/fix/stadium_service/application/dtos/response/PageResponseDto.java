package com.fix.stadium_service.application.dtos.response;

import com.fix.stadium_service.domain.model.Stadium;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class PageResponseDto {
    private List<StadiumResponseDto> content;
    private int page;         // 현재 페이지 (0부터 시작)
    private int pageSize;     // 페이지 크기
    private int totalPages;   // 전체 페이지 수
    private long totalElements; // 전체 데이터 수

    public PageResponseDto(List<Stadium> stadiums, int page, int pageSize, long totalElements) {
        this.content = stadiums.stream()
                .map(StadiumResponseDto::new)
                .toList();
        this.page = page;
        this.pageSize = pageSize;
        this.totalElements = totalElements;
        this.totalPages = (int) Math.ceil((double) totalElements / pageSize);
    }
}












