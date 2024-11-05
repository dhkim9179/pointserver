package com.example.pointserver.history.repository;

import com.example.pointserver.common.enums.PointAction;
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
    public HistoryInfo.Earn findHistoryForEarnCancel(long memberId, String transactionId) {
        return jpaQueryFactory
                .select(new QHistoryInfo_Earn(
                        memberPointExpire.id,
                        memberPointHistory.memberId,
                        memberPointExpire.expireDay,
                        memberPointHistory.amount,
                        memberPointExpire.expireAmount
                ))
                .from(memberPointHistory)
                .join(memberPointExpire).on(memberPointHistory.memberId.eq(memberPointExpire.memberId)).on(memberPointHistory.transactionId.eq(memberPointExpire.transactionId))
                .where(memberPointHistory.transactionId.eq(transactionId))
                .where(memberPointHistory.memberId.eq(memberId))
                .where(memberPointHistory.action.eq("earn"))
                .fetchOne();
    }

    @Override
    public HistoryInfo.Use findHistoryForUseCancel(long memberId, String transactionId) {
        return jpaQueryFactory
                .select(new QHistoryInfo_Use(
                        memberPointHistory.amount
                ))
                .from(memberPointHistory)
                .where(memberPointHistory.transactionId.eq(transactionId))
                .where(memberPointHistory.memberId.eq(memberId))
                .where(memberPointHistory.action.eq(PointAction.USE.getCode()))
                .fetchOne();
    }
}
