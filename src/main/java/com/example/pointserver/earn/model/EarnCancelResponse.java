package com.example.pointserver.earn.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EarnCancelResponse {
    private String transactionId;
    private int balance;
}
