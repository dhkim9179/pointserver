package com.example.pointserver.history.repository;

import com.example.pointserver.history.model.HistoryInfo;
import com.example.pointserver.history.model.QHistoryInfo_Earn;
import com.example.pointserver.history.model.QHistoryInfo_Use;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import static com.example.pointserver.common.entity.history.QMemberPointExpire.memberPointExpire;
import static com.example.pointserver.common.entity.history.QMemberPointHistory.memberPointHistory;

@Repository
@RequiredArgsConstructor
public class HistoryRepositoryCustomImpl implements HistoryRepositoryCustom {
    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public HistoryInfo.Earn findHistoryForEarnCancel(long memberId, String orderNo) {
        return jpaQueryFactory
                .select(new QHistoryInfo_Earn(
                        memberPointExpire.id,
                        memberPointHistory.memberId,
                        memberPointExpire.expireDay,
                        memberPointHistory.amount,
                        memberPointExpire.expireAmount
                ))
                .from(memberPointHistory)
                .join(memberPointExpire).on(memberPointHistory.memberId.eq(memberPointExpire.memberId)).on(memberPointHistory.orderNo.eq(memberPointExpire.orderNo))
                .where(memberPointHistory.orderNo.eq(orderNo))
                .where(memberPointHistory.memberId.eq(memberId))
                .fetchOne();
    }

    @Override
    public HistoryInfo.Use findHistoryForUseCancel(long memberId, String orderNo) {
        return jpaQueryFactory
                .select(new QHistoryInfo_Use(
                        memberPointHistory.amount
                ))
                .from(memberPointHistory)
                .where(memberPointHistory.orderNo.eq(orderNo))
                .where(memberPointHistory.memberId.eq(memberId))
                .fetchOne();
    }
}
