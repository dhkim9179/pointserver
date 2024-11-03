package com.example.pointserver.common.enums;

import lombok.Getter;

@Getter
public enum CancelType {
    ALL("all"),
    PARTIAL("partial")

    ;

    private final String code;
    CancelType(String code) {
        this.code = code;
    }
}
