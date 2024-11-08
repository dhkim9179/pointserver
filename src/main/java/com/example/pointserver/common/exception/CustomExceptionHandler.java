package com.example.pointserver.common.exception;

import com.example.pointserver.common.response.ErrorResponse;
import com.example.pointserver.common.response.ResponseCode;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.DateTimeException;

@RestControllerAdvice
public class CustomExceptionHandler {

    @ExceptionHandler(value = CustomException.class)
    public ErrorResponse customExceptionHadnler(CustomException cex, HttpServletResponse response) {
        response.setStatus(cex.getStatus());
        return ErrorResponse.builder()
                .code(cex.getCode())
                .message(cex.getMessage())
                .detail(cex.getDetail())
                .build();
    }

    @ExceptionHandler(value = Exception.class)
    public ErrorResponse exceptionHandler(Exception ex, HttpServletResponse response) {
        ResponseCode responseCode = ResponseCode.UNDEFINED_EXCEPTION;

        // exception에 따라 response code 및 http status를 셋팅
        if (ex instanceof MissingServletRequestParameterException ||
            ex instanceof BindException ||
            ex instanceof IllegalArgumentException ||
            ex instanceof DateTimeException ||
            ex instanceof MethodArgumentTypeMismatchException ||
            ex instanceof HttpMessageNotReadableException
        ) {
            responseCode = ResponseCode.MISSING_REQUIRED_PARAMETER;
        } else if (ex instanceof DataIntegrityViolationException) {
            responseCode = ResponseCode.DUPLICATE_TRANSACTION_ID;
        }

        response.setStatus(responseCode.getStatus());
        return ErrorResponse.builder()
                .code(responseCode.getCode())
                .message(responseCode.getMessage())
                .detail(ex.getMessage())
                .build();
    }
}
