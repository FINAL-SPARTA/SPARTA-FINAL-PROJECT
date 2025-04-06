package com.fix.user_service.application.dtos.request;

import com.fix.common_service.entity.UserRole;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@Getter
@Builder
@NoArgsConstructor(force = true)
@AllArgsConstructor
public class UserSearchRequestDto {

    @Size(min = 1, max = 50, message = "검색어는 1~50자 이내여야 합니다.")
    private final String keyword;  // username, email, nickname 등에 대한 통합 검색어

    private final UserRole role;   // 권한별 검색 필터

    private final Pageable pageable; // 페이지네이션 정보 포함

//     ✅ null-safe + 기본값 처리 포함한 팩토리 메서드
    public static UserSearchRequestDto of(String keyword, UserRole role, Pageable pageable) {
        String safeKeyword = (keyword != null && !keyword.trim().isBlank()) ? keyword.trim() : null;
        Pageable safePageable = (pageable != null) ? pageable : PageRequest.of(0, 20); // 기본 0페이지, 20개

        return UserSearchRequestDto.builder()
            .keyword(safeKeyword)
            .role(role)
            .pageable(safePageable)
            .build();
    }
}
