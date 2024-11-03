package com.example.pointserver.common.enums;

import lombok.Getter;

@Getter
public enum PointAction {
    EARN("earn"),
    USE("use"),
    ;

    private final String code;
    PointAction(String code) {
        this.code = code;
    }

}
