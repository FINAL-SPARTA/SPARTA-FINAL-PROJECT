package com.fix.user_service.application.dtos.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
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

    @NotBlank(message = "전화 번호는 필수 입력값입니다.")
    @Pattern(regexp = "^010\\d{8}$", message = "전화번호는 010으로 시작하고 숫자 11자리여야 합니다.")
    private String phoneNumber;
}
