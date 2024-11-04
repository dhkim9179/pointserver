package com.example.pointserver.expire;

import com.example.pointserver.common.entity.history.MemberPointExpire;
import com.example.pointserver.expire.repository.ExpireRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ExpireService {
    private final ExpireRepository expireRepository;

    /**
     * 소멸 저장
     * @param memberId 회원 아이디
     * @param orderNo 주문 번호
     * @param expireDay 소멸일
     * @param expireAmount 소멸금액
     * @param isAdmin 관리자 여부
     */
    public void insertExpire(
            long memberId,
            String orderNo,
            LocalDate expireDay,
            int expireAmount,
            boolean isAdmin
    ) {
        MemberPointExpire memberPointExpire = new MemberPointExpire();
        memberPointExpire.setMemberId(memberId);
        memberPointExpire.setOrderNo(orderNo);
        memberPointExpire.setExpireDay(expireDay);
        memberPointExpire.setExpireAmount(expireAmount);
        memberPointExpire.setAdmin(isAdmin);

        expireRepository.save(memberPointExpire);
    }

    public void increaseExpireAmount(long id, int amount) {
        expireRepository.increaseExpireAmount(id, amount);
    }

    public void decreaseExpireAmount(long id, int amount) {
        expireRepository.decreaseExpireAmount(id, amount);
    }

    public List<MemberPointExpire> findExpires(long memberId) {
        return expireRepository.findExpires(memberId);
    }
}
