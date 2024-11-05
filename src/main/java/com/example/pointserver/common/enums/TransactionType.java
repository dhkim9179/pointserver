package com.example.pointserver.common.enums;

import lombok.Getter;

@Getter
public enum TransactionType {
    ORDER("order"),
    EVENT("event"),
    PROMOTION("promotion"),
    ADMIN("admin"),
    ;

    private final String code;
    TransactionType(String code) {
        this.code = code;
    }
}