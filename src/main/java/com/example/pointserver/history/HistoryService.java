package com.example.pointserver.history;

import com.example.pointserver.common.entity.history.MemberPointHistory;
import com.example.pointserver.history.model.HistoryInfo;
import com.example.pointserver.history.repository.HistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class HistoryService {
    private final HistoryRepository historyRepository;

    /**
     * 이력 저장
     * @param memberId 회원 아이디
     * @param orderNo 주문번호
     * @param action 포인트 동작
     * @param amount 금액
     * @param description 상세설명
     * @return 이력 아이디
     */
    public long insertHistory(
            long memberId,
            String orderNo,
            String action,
            int amount,
            String description
    ) {
        MemberPointHistory memberPointHistory = new MemberPointHistory();
        memberPointHistory.setMemberId(memberId);
        memberPointHistory.setOrderNo(orderNo);
        memberPointHistory.setAction(action);
        memberPointHistory.setAmount(amount);
        memberPointHistory.setDescription(description);

        return historyRepository.save(memberPointHistory).getId();
    }

    /**
     * 적립 취소를 위한 이력 조회
     * @param memberId 회원 아이디
     * @param orderNo 주문 번호
     * @return 이력
     */
    public HistoryInfo.Earn findHistoryForEarnCancel(long memberId, String orderNo) {
        return historyRepository.findHistoryForEarnCancel(memberId, orderNo);
    }

    /**
     * 사용 취소를 위한 이력 조회
     * @param memberId 회원 아이디
     * @param orderNo 주문 번호
     * @return 이력
     */
    public HistoryInfo.Use findHistoryForUseCancel(long memberId, String orderNo) {
        return historyRepository.findHistoryForUseCancel(memberId, orderNo);
    }
}
