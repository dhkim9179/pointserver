package com.example.pointserver.history.model;

import com.querydsl.core.annotations.QueryProjection;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

public class HistoryInfo {

    @Data
    @NoArgsConstructor
    public static class Earn {
        private long expireId;
        private long memberId;
        private LocalDate expireDay;
        private int amount;
        private int expireAmount;

        @QueryProjection
        public Earn(
                long expireId,
                long memberId,
                LocalDate expireDay,
                int amount,
                int expireAmount
        ) {
            this.expireId = expireId;
            this.memberId = memberId;
            this.expireDay = expireDay;
            this.amount = amount;
            this.expireAmount = expireAmount;
        }
    }

    @Data
    @NoArgsConstructor
    public static class Use {
        private int amount;

        @QueryProjection
        public Use(int amount) {
            this.amount = amount;
        }
    }
}
