package com.fix.user_service.application.dtos.request;

import com.fix.common_service.entity.UserRole;
import jakarta.validation.constraints.*;
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

    @NotBlank(message = "전화 번호는 필수 입력값입니다.")
    @Pattern(regexp = "^010\\d{8}$", message = "전화번호는 010으로 시작하고 숫자 11자리여야 합니다.")
    private String phoneNumber;

}
