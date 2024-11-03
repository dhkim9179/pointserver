package com.example.pointserver.use.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import static com.example.pointserver.common.entity.QMemberPoint.memberPoint;

@Repository
@RequiredArgsConstructor
public class UseRepositoryCustomImpl implements UseRepositoryCustom{
    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public Integer findBalance(long memberId) {
        return jpaQueryFactory
                .select(memberPoint.balance)
                .from(memberPoint)
                .where(memberPoint.memberId.eq(memberId))
                .fetchOne();
    }

    @Override
    @Transactional
    public void increaseBalance(long memberId, int point) {
        jpaQueryFactory
                .update(memberPoint)
                .set(memberPoint.balance, memberPoint.balance.add(point))
                .where(memberPoint.memberId.eq(memberId))
                .execute();
    }

    @Override
    @Transactional
    public void decreaseBalance(long memberId, int point) {
        jpaQueryFactory
                .update(memberPoint)
                .set(memberPoint.balance, memberPoint.balance.subtract(point))
                .where(memberPoint.memberId.eq(memberId))
                .execute();
    }
}
