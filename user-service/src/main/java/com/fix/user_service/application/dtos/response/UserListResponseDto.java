package com.fix.user_service.application.dtos.response;

import com.fix.user_service.domain.User;
import lombok.Getter;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.stream.Collectors;

@Getter
public class UserListResponseDto {

    private final List<UserDetailResponseDto> users;
    private final long totalElements;
    private final int totalPages;
    private final int currentPage;

    public UserListResponseDto(Page<User> page) {
        this.users = page.getContent().stream()
            .map(UserDetailResponseDto::from)
            .collect(Collectors.toList());
        this.totalElements = page.getTotalElements();
        this.totalPages = page.getTotalPages();
        this.currentPage = page.getNumber() + 1;
    }
}
