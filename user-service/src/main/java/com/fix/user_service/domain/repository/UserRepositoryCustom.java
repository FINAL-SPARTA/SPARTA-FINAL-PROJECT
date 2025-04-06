package com.fix.user_service.domain.repository;

import com.fix.user_service.application.dtos.request.UserSearchCondition;
import com.fix.user_service.application.dtos.request.UserSearchRequestDto;
import com.fix.user_service.domain.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserRepositoryCustom {

    // 사용자용 간단 검색 (keyword + role + pageable)
    Page<User> searchByKeyword(UserSearchRequestDto requestDto);

    // 관리자용 고급 필터 검색 (username, email, nickname, role + pageable)
    Page<User> searchByCondition(UserSearchCondition condition, Pageable pageable);
}
