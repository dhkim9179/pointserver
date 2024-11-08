package com.example.pointserver.expire.repository;

import com.example.pointserver.common.entity.history.MemberPointExpire;

import java.time.LocalDate;
import java.util.List;

public interface ExpireRepositoryCustom {
    List<MemberPointExpire> findExpires(long memberId);
    void increaseExpireAmount(long id, int amount);
    void decreaseExpireAmount(long id, int amount);
    void updateExpireDayForTest(long memberId, String transactionId, LocalDate expireDay);
}
