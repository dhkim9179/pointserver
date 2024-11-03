package com.example.pointserver.common.response;

import java.util.EnumSet;

public interface ResponseCodeTemplate {
    int getCode();
    String getMessage();
    int getStatus();

    static <E extends Enum<E> & ResponseCodeTemplate> E valueOfCode(Class<E> enumClass, int code) {
        for (E e : EnumSet.allOf(enumClass)) {
            if (e.getCode() == code) {
                return e;
            }
        }
        return null;
    }
}
