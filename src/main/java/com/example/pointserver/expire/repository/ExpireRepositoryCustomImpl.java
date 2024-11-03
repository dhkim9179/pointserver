package com.example.pointserver.expire.repository;

import com.example.pointserver.common.entity.history.MemberPointExpire;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.example.pointserver.common.entity.history.QMemberPointExpire.memberPointExpire;

@Repository
@RequiredArgsConstructor
public class ExpireRepositoryCustomImpl implements ExpireRepositoryCustom {
    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public List<MemberPointExpire> findExpires(long memberId) {
        return jpaQueryFactory
                .selectFrom(memberPointExpire)
                .where(memberPointExpire.memberId.eq(memberId))
                .orderBy(memberPointExpire.isAdmin.desc(), memberPointExpire.expireDay.asc()) // 관리자 먼저, 만료일이 짧은 순
                .fetch();
    }

    @Override
    @Transactional
    public void updateExpire(long id, int amount) {
        jpaQueryFactory
                .update(memberPointExpire)
                .set(memberPointExpire.expireAmount, memberPointExpire.expireAmount.subtract(amount))
                .where(memberPointExpire.id.eq(id))
                .execute();
    }
}
