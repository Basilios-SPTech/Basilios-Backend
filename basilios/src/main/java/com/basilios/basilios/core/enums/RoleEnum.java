package com.basilios.basilios.core.enums;

public enum RoleEnum {
    ROLE_CLIENTE("ROLE_CLIENTE"),
    ROLE_FUNCIONARIO("ROLE_FUNCIONARIO");

    private final String value;

    RoleEnum(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}