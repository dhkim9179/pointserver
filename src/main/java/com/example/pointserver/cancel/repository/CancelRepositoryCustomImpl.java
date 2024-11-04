package com.example.pointserver.cancel.repository;

import com.example.pointserver.common.entity.cancel.MemberPointCancel;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import static com.example.pointserver.common.entity.cancel.QMemberPointCancel.memberPointCancel;

@Repository
@RequiredArgsConstructor
public class CancelRepositoryCustomImpl implements CancelRepositoryCustom {
    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public MemberPointCancel findCancel(long memberId, String orderNo) {
        return jpaQueryFactory
                .selectFrom(memberPointCancel)
                .where(memberPointCancel.orderNo.eq(orderNo))
                .where(memberPointCancel.memberId.eq(memberId))
                .fetchOne();
    }

    @Override
    @Transactional
    public void updateCancel(long cancelId, int amount, int cancelableAmount) {
        jpaQueryFactory
                .update(memberPointCancel)
                .set(memberPointCancel.amount, memberPointCancel.amount.add(amount))
                .set(memberPointCancel.cancelableAmount, cancelableAmount)
                .where(memberPointCancel.id.eq(cancelId))
                .execute();
    }
}
