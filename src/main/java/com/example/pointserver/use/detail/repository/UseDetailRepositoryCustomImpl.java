package com.example.pointserver.use.detail.repository;

import com.example.pointserver.use.detail.model.QUseDetail;
import com.example.pointserver.use.detail.model.UseDetail;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.example.pointserver.common.entity.history.QMemberPointExpire.memberPointExpire;
import static com.example.pointserver.common.entity.history.QMemberPointHistory.memberPointHistory;
import static com.example.pointserver.common.entity.history.QMemberPointUsageDetail.memberPointUsageDetail;

@Repository
@RequiredArgsConstructor
public class UseDetailRepositoryCustomImpl implements UseDetailRepositoryCustom {
    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public List<UseDetail> findUsageDetail(String orderNo) {
        return jpaQueryFactory
                .select(
                        new QUseDetail(
                                memberPointUsageDetail.id,
                                memberPointExpire.id, // 다시 채워주는 경우를 위한 id
                                memberPointExpire.expireDay, // 만료일 확인
                                memberPointUsageDetail.amount // 쓴만큼 다시 채워주기
                        )
                )
                .from(memberPointUsageDetail)
                .join(memberPointExpire).on(memberPointUsageDetail.memberPointExpireId.eq(memberPointExpire.id))
                .join(memberPointHistory).on(memberPointUsageDetail.memberPointHistoryId.eq(memberPointHistory.id))
                .where(memberPointHistory.orderNo.eq(orderNo))
                .orderBy(memberPointExpire.expireDay.asc())
                .orderBy(memberPointUsageDetail.memberPointExpireId.asc())
                .fetch();

    }

    @Override
    @Transactional
    public void updateUsageDetail(long id, int amount) {
        jpaQueryFactory
                .update(memberPointUsageDetail)
                .set(memberPointUsageDetail.amount, memberPointUsageDetail.amount.subtract(amount))
                .where(memberPointUsageDetail.id.eq(id))
                .execute();
    }
}
