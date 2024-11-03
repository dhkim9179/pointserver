package com.example.pointserver.expire.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ExpireUpdate {
    private long expireId;
    private int amount;
    private String orderNo;
}
