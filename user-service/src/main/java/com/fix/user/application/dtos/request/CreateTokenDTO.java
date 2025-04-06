package com.fix.user.application.dtos.request;

import com.fix.common_service.entity.UserRole;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CreateTokenDTO {
    private Long userId;
    private String username;
    private UserRole role;
}
