package com.amazon.enums;

import lombok.Getter;

@Getter
public enum CompanyStatus {

    ACTIVE("Active"),PASSIVE("Passive");

    private final String value;

    public String getValue() {
        return value;
    }

    CompanyStatus(String value) {
        this.value = value;
    }

}
