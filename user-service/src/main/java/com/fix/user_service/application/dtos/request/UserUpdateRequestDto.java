package com.fix.user_service.application.dtos.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserUpdateRequestDto {

    @NotBlank(message = "Nickname은 필수입니다.")
    private String nickname;

    @Email(message = "이메일 형식이 아닙니다.")
    @NotBlank(message = "Email은 필수입니다.")
    private String email;

    private String roleName; // 선택사항: 역할도 변경 가능하도록
}
