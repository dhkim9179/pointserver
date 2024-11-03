package com.example.pointserver.common.exception;

import com.example.pointserver.common.response.ResponseCode;
import lombok.Getter;

import java.io.Serial;

@Getter
public class CustomException extends RuntimeException {
    @Serial
    private static final long serialVersionUID = 1L;

    private final int code;
    private final String message;
    private final Object detail;
    private final int status;

    public CustomException(ResponseCode response) {
        super(response.getMessage());
        this.code = response.getCode();
        this.message = response.getMessage();
        this.detail = "";
        this.status = response.getStatus();
    }

    public CustomException(ResponseCode response, Object detail) {
        super(response.getMessage());
        this.code = response.getCode();
        this.message = response.getMessage();
        this.detail = detail;
        this.status = response.getStatus();
    }
}
