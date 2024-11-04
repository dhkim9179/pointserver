package com.example.pointserver.earn.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

import java.time.LocalDate;

public class Earn {

    @Data
    public static class Request {
        @NotNull(message = "필수 입력값입니다")
        @Positive(message = "유효한 회원 아이디를 입력해주세요")
        private long memberId;

        @NotNull(message = "필수 입력값입니다")
        @Positive(message = "유효한 금액을 입력해주세요")
        private int point;

        @NotEmpty(message = "필수 입력값입니다")
        private String orderNo;

        @NotEmpty(message = "필수 입력값입니다")
        @Length(min = 1, max = 200, message = "200자 이내로 작성해주세요")
        private String description;

        private LocalDate expireDay;

        @NotNull(message = "필수 입력값입니다")
        @JsonProperty("isAdmin")
        private boolean isAdmin;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Response {
        private String orderNo;
        private int balance;
    }
}
