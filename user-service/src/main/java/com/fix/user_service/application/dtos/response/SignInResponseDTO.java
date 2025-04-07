package com.fix.user_service.application.dtos.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SignInResponseDTO {
    private String token;
    private Long userId;
    private String username;
    private String role;
}
