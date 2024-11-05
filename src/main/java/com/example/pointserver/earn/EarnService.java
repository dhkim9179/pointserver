package com.example.pointserver.earn;

import com.example.pointserver.cancel.CancelService;
import com.example.pointserver.cancel.history.CancelHistoryService;
import com.example.pointserver.common.entity.MemberPoint;
import com.example.pointserver.common.entity.cancel.MemberPointCancel;
import com.example.pointserver.common.entity.history.MemberPointHistory;
import com.example.pointserver.common.enums.CancelType;
import com.example.pointserver.common.enums.PointAction;
import com.example.pointserver.common.enums.TransactionType;
import com.example.pointserver.earn.repository.EarnRepository;
import com.example.pointserver.expire.ExpireService;
import com.example.pointserver.history.HistoryService;
import com.example.pointserver.history.model.HistoryInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EarnService {
    private final EarnRepository earnRepository;
    private final HistoryService historyService;
    private final ExpireService expireService;
    private final CancelService cancelService;
    private final CancelHistoryService cancelHistoryService;

    /**
     * 잔액 조회
     * @param memberId 회원 아이디
     * @return member_point.history
     */
    public Integer findBalance(long memberId) {
        return earnRepository.findBalance(memberId);
    }

    /**
     * 거래번호 중복 확인
     * @param transactionId
     * @return
     */
    public boolean isDuplicateTransactionId(String transactionId) {
        List<MemberPointHistory> memberPointHistories = historyService.findHistory(transactionId);
        return !memberPointHistories.isEmpty();
    }

    /**
     * 취소 조회
     * @param memberId 회원 아이디
     * @param transactionId 거래번호
     * @return member_point_cancel
     */
    public MemberPointCancel findCancel(long memberId, String transactionId) {
        return cancelService.findCancel(memberId, transactionId);
    }

    /**
     * 적립 취소를 위한 이력 조회
     * @param memberId 회원아이디
     * @param transactionId 거래번호
     * @return 이력
     */
    public HistoryInfo.Earn findHistory(long memberId, String transactionId) {
        return historyService.findHistoryForEarnCancel(memberId, transactionId);
    }

    /**
     * 적립
     * @param memberId 회원 아이디
     * @param transactionId 주문번호
     * @param amount 금액
     * @param expireDay 만료일
     * @param description 상세설명
     * @param isNewUser 신규회원여부
     */
    @Transactional(rollbackFor = {Exception.class})
    public void earn(
            long memberId,
            String transactionId,
            String transactionType,
            int amount,
            LocalDate expireDay,
            String description,
            boolean isNewUser
    ) {
        // 잔액 업데이트
        if (isNewUser) {
            MemberPoint memberPoint = new MemberPoint();
            memberPoint.setMemberId(memberId);
            memberPoint.setBalance(amount);
            earnRepository.save(memberPoint);
        } else {
            earnRepository.increaseBalance(memberId, amount);
        }

        // 이력 저장
        historyService.insertHistory(
                memberId,
                transactionId,
                PointAction.EARN,
                transactionType,
                amount,
                description
        );

        // 소멸 저장
        boolean isAdmin = false;
        if (transactionType.equals(TransactionType.ADMIN.getCode())) {
            isAdmin = true;
        }
        expireService.insertExpire(
                memberId,
                transactionId,
                expireDay,
                amount,
                isAdmin
        );
    }

    /**
     * 적립 취소
     * @param memberId 회원 아이디
     * @param transactionId 거래번호
     * @param expireId 만료일
     * @param amount 금액
     * @param description 상세설명
     */
    @Transactional(rollbackFor = {Exception.class})
    public void cancel(long memberId, String transactionId, long expireId, int amount, String description) {
        // 잔액 차감
        earnRepository.decreaseBalance(memberId, amount);

        // 소멸 금액 차감
        expireService.decreaseExpireAmount(expireId, amount);

        // 취소 저장
        long cancelId = cancelService.insertCancel(
                memberId,
                transactionId,
                PointAction.EARN,
                CancelType.ALL,
                amount,
                0
        );

        cancelHistoryService.insertHistory(
                cancelId,
                amount,
                description
        );
    }
}
