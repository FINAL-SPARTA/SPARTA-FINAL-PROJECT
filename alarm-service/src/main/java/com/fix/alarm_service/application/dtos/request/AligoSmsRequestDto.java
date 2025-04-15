package com.fix.alarm_service.application.dtos.request;


import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AligoSmsRequestDto {
    private String receiver;
    private String msg;
}
