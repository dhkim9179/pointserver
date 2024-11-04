package com.example.pointserver.common.response;

import com.fasterxml.jackson.annotation.JsonValue;
import org.springframework.http.HttpStatus;

public enum ResponseCode implements ResponseCodeTemplate {
    MISSING_REQUIRED_PARAMETER(100, "Missing required parameter", HttpStatus.BAD_REQUEST.value()),
    JSON_DATA_FORMAT_ERROR(101, "json data format error", HttpStatus.BAD_REQUEST.value()),

    // 1회 적립 금액 초과
    POINT_EXCEED_LIMIT(200, "point exceed limit", HttpStatus.BAD_REQUEST.value()),
    // 잔액 초과
    BALANCE_EXCEED_LIMIT(201, "balance exceed limit", HttpStatus.BAD_REQUEST.value()),
    // 만료일 유효성
    INVALID_EXPIRE_PERIOD(202, "invalid expire period", HttpStatus.BAD_REQUEST.value()),
    // 잔액이 없는 경우
    NO_BALANCE(203, "no balance", HttpStatus.BAD_REQUEST.value()),
    // 잔액이 사용금액보다 작은 경우
    INSUFFICIENT_BALANCE(204, "insufficient balance", HttpStatus.BAD_REQUEST.value()),
    DUPLICATE_ORDER_NO(205, "duplicate order no", HttpStatus.CONFLICT.value()),

    // 이력이 없는 경우
    NO_HISTORY(300, "no history", HttpStatus.BAD_REQUEST.value()),
    // 만료일이 지난 경우
    EXPIRED(301, "expired", HttpStatus.BAD_REQUEST.value()),

    // 이미 취소한 경우
    ALREADY_CANCELED(400, "already canceled", HttpStatus.BAD_REQUEST.value()),
    // 적립취소 시 이미 금액을 사용한 경우
    ALREADY_USE_AMOUNT(401, "already use amount", HttpStatus.BAD_REQUEST.value()),
    // 이력 금액과 취소 금액이 다른 경우
    INVALID_CANCEL_AMOUNT(402, "invalid cancel amount", HttpStatus.BAD_REQUEST.value()),
    // 부분취소로 요청했으나, 전체취소로 요청해야하는 경우
    REQUIRES_FULL_CANCEL(403, "requires full cancel", HttpStatus.BAD_REQUEST.value()),
    // 취소 금액이 없는 경우
    NO_CANCEL_AMOUNT(404, "no cancel amount", HttpStatus.BAD_REQUEST.value()),

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
