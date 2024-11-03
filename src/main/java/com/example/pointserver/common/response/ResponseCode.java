package com.example.pointserver.common.response;

import com.fasterxml.jackson.annotation.JsonValue;
import org.springframework.http.HttpStatus;

public enum ResponseCode implements ResponseCodeTemplate {
    MISSING_REQUIRED_PARAMETER(100, "Missing required parameter", HttpStatus.BAD_REQUEST.value()),
    JSON_DATA_FORMAT_ERROR(101, "json data format error", HttpStatus.BAD_REQUEST.value()),
    UNDEFINED_EXCEPTION(999, "undefined exception", HttpStatus.INTERNAL_SERVER_ERROR.value()),
    ;

    private final int code;
    private final String message;
    private final int status;

    ResponseCode(int code, String message, int status) {
        this.code = code;
        this.message = message;
        this.status = status;
    }

    @Override
    @JsonValue
    public int getCode() {
        return code;
    }

    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public int getStatus() {
        return status;
    }
}
