package com.fix.user_service.application.dtos.response;

import com.fix.user_service.domain.User;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class UserDetailResponseDto {

    private final Long userId;
    private final String username;
    private final String nickname;
    private final String email;
    private final String roleName;
    private final String phoneNumber;
    private final Integer point;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private final LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private final LocalDateTime updatedAt;

    public static UserDetailResponseDto from(User user) {
        return UserDetailResponseDto.builder()
            .userId(user.getUserId())
            .username(user.getUsername())
            .nickname(user.getNickname())
            .email(user.getEmail())
            .roleName(user.getRoleName().name())
            .phoneNumber(user.getPhoneNumber())
            .point(user.getPoint())
            .createdAt(user.getCreatedAt())
            .updatedAt(user.getUpdatedAt())
            .build();
    }
}
