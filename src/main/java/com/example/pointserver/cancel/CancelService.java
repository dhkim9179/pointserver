package com.example.pointserver.cancel;

import com.example.pointserver.cancel.repository.CancelRepository;
import com.example.pointserver.common.entity.cancel.MemberPointCancel;
import com.example.pointserver.common.enums.CancelType;
import com.example.pointserver.common.enums.PointAction;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CancelService {
    private final CancelRepository cancelRepository;

    /**
     * 취소 조회
     * @param memberId 회원 아이디
     * @param orderNo 주문번호
     * @return member_point_cancel
     */
    public MemberPointCancel findCancel(long memberId, String orderNo) {
        return cancelRepository.findCancel(memberId, orderNo);
    }

    /**
     * member_point_cancel 데이터 생성
     * @param memberId 회원 아이디
     * @param orderNo 주문번호
     * @param action 포인트 동작
     * @param type 취소동작
     * @param amount 금액
     * @param cancelableAmount 앞으로 취소 가능한 금액
     * @return 취소 아이디
     */
    public long insertCancel(
            long memberId,
            String orderNo,
            PointAction action,
            CancelType type,
            int amount,
            int cancelableAmount
    ) {
        MemberPointCancel memberPointCancel = new MemberPointCancel();
        memberPointCancel.setMemberId(memberId);
        memberPointCancel.setOrderNo(orderNo);
        memberPointCancel.setAction(action.getCode());
        memberPointCancel.setType(type.getCode());
        memberPointCancel.setAmount(amount);
        memberPointCancel.setCancelableAmount(cancelableAmount);

        return cancelRepository.save(memberPointCancel).getId();
    }

    /**
     * 취소 업데이트
     * @param cancelId 취소 아이디
     * @param cancelableAmount 앞으로 취소 가능한 금액
     */
    public void updateCancel(long cancelId, int cancelableAmount) {
        cancelRepository.updateCancel(cancelId, cancelableAmount);
    }
}
