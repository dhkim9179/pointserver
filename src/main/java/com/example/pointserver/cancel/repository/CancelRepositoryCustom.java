package com.example.pointserver.cancel.repository;

import com.example.pointserver.common.entity.cancel.MemberPointCancel;

public interface CancelRepositoryCustom {
    MemberPointCancel findCancel(long memberId, String transactionId);
    void updateCancel(long cancelId, int amount, int cancelableAmount);
}
