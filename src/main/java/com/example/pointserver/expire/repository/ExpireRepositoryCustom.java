package com.example.pointserver.expire.repository;

import com.example.pointserver.common.entity.history.MemberPointExpire;

import java.util.List;

public interface ExpireRepositoryCustom {
    List<MemberPointExpire> findExpires(long memberId);
    void updateExpire(long id, int amount);
}
