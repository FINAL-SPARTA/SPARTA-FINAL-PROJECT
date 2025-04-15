package com.fix.user_service.application.dtos.response;

import lombok.Getter;

@Getter
public class PhoneNumberResponseDto {

    private String phoneNumber;

    public PhoneNumberResponseDto(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
}
