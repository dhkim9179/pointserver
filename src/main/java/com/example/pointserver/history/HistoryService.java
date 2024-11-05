package com.example.pointserver.history;

import com.example.pointserver.common.entity.history.MemberPointHistory;
import com.example.pointserver.common.enums.PointAction;
import com.example.pointserver.common.enums.TransactionType;
import com.example.pointserver.history.model.HistoryInfo;
import com.example.pointserver.history.repository.HistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class HistoryService {
    private final HistoryRepository historyRepository;

    /**
     * 이력 저장
     * @param memberId 회원 아이디
     * @param transactionId 주문번호
     * @param action 포인트 동작
     * @param amount 금액
     * @param description 상세설명
     * @return 이력 아이디
     */
    public long insertHistory(
            long memberId,
            String transactionId,
            PointAction action,
            String transactionType,
            int amount,
            String description
    ) {
        MemberPointHistory memberPointHistory = new MemberPointHistory();
        memberPointHistory.setMemberId(memberId);
        memberPointHistory.setTransactionId(transactionId);
        memberPointHistory.setTransactionType(transactionType);
        memberPointHistory.setAction(action.getCode());
        memberPointHistory.setAmount(amount);
        memberPointHistory.setDescription(description);

        return historyRepository.save(memberPointHistory).getId();
    }

    /**
     * 이력 조회
     * @param transactionId 주문번호
     * @return 이력
     */
    public List<MemberPointHistory> findHistory(String transactionId) {
        return historyRepository.findByTransactionId(transactionId);
    }

    /**
     * 적립 취소를 위한 이력 조회
     * @param memberId 회원 아이디
     * @param transactionId 거래 번호
     * @return 이력
     */
    public HistoryInfo.Earn findHistoryForEarnCancel(long memberId, String transactionId) {
        return historyRepository.findHistoryForEarnCancel(memberId, transactionId);
    }

    /**
     * 사용 취소를 위한 이력 조회
     * @param memberId 회원 아이디
     * @param transactionId 거래 번호
     * @return 이력
     */
    public HistoryInfo.Use findHistoryForUseCancel(long memberId, String transactionId) {
        return historyRepository.findHistoryForUseCancel(memberId, transactionId);
    }
}
