package com.fix.user_service.application.dtos.request;

import com.fix.common_service.entity.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserCreateRequestDto {

    @NotBlank(message = "Username은 필수입니다.")
    @Size(min = 4, max = 10, message = "Username은 4~10자로 입력해야 합니다.")
    private String username;

    @NotBlank(message = "비밀번호는 필수입력값입니다.")
    private String password;

    @NotBlank(message = "닉네이음 필수 입력값입니다.")
    private String nickname;

    @Email(message = "이메일 형식이 아닙니다.")
    @NotBlank(message = "Email은 필수 입력값입니다.")
    private String email;

    @NotNull(message = "역할은 필수 입력값입니다.")
    private UserRole roleName;
}
