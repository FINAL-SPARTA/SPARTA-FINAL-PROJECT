package com.fix.user_service.application.dtos.request;

import com.fix.common_service.entity.UserRole;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserSearchCondition {

    private String username;
    private String email;
    private String nickname;
    private UserRole role;
}
