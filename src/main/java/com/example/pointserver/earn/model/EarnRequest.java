package com.example.pointserver.earn.model;

import com.example.pointserver.common.enums.TransactionType;
import com.example.pointserver.common.enums.validation.ValidTransactionType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EarnRequest {
    @NotNull(message = "필수 입력값입니다")
    @Positive(message = "유효한 회원 아이디를 입력해주세요")
    @Schema(description = "회원아이디", example = "123456")
    private long memberId;

    @NotNull(message = "필수 입력값입니다")
    @Positive(message = "유효한 금액을 입력해주세요")
    @Schema(description = "포인트금액")
    private int point;

    @NotEmpty(message = "필수 입력값입니다")
    @Schema(description = "거래번호", example = "order_001")
    private String transactionId;

    @ValidTransactionType(enumClass = TransactionType.class, message = "지원하지 않는 거래구분값 입니다.")
    @Schema(description = "거래구분(order, event, promotion, admin)", example = "order")
    private String transactionType;

    @NotEmpty(message = "필수 입력값입니다")
    @Length(min = 1, max = 200, message = "200자 이내로 작성해주세요")
    @Schema(description = "적립 상세설명", example = "주문적립")
    private String description;

    @Schema(description = "만료일", nullable = true, example = "2024-12-01")
    private LocalDate expireDay;
}
