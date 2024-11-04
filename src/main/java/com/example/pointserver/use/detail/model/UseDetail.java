package com.example.pointserver.use.detail.model;

import com.querydsl.core.annotations.QueryProjection;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
public class UseDetail {
    private long usageDetailId;
    private long expireId;
    private LocalDate expireDay;
    private int useAmount;

    @QueryProjection
    public UseDetail(long usageDetailId, long expireId, LocalDate expireDay, int useAmount) {
        this.usageDetailId = usageDetailId;
        this.expireId = expireId;
        this.expireDay = expireDay;
        this.useAmount = useAmount;
    }
}
