package com.fix.payments_service.application.dtos.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class TossCancelRequestDto {

    private String cancelReason;

    // 선택 필드: 부분 환불용
    private Integer cancelAmount;
}
