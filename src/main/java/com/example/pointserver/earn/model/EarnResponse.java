package com.example.pointserver.earn.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EarnResponse {
    @Schema(description = "거래번호")
    private String transactionId;

    @Schema(description = "적립 후 잔액")
    private int balance;
}
