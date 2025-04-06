package com.fix.common_service.entity;

import lombok.Getter;

@Getter
public enum UserRole {
    MASTER("ROLE_MASTER"),
    MANAGER("ROLE_MANAGER"),
    CUSTOMER("ROLE_CUSTOMER");

    private final String authority;

    UserRole(String authority) {
        this.authority = authority;
    }

}
