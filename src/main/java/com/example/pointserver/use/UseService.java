package com.example.pointserver.use;

import com.example.pointserver.cancel.CancelService;
import com.example.pointserver.cancel.history.CancelHistoryService;
import com.example.pointserver.common.entity.history.MemberPointExpire;
import com.example.pointserver.common.entity.history.MemberPointHistory;
import com.example.pointserver.common.enums.CancelType;
import com.example.pointserver.common.enums.PointAction;
import com.example.pointserver.expire.ExpireService;
import com.example.pointserver.expire.model.ExpireUpdate;
import com.example.pointserver.history.HistoryService;
import com.example.pointserver.history.model.HistoryInfo;
import com.example.pointserver.use.detail.UseDetailService;
import com.example.pointserver.use.detail.model.UseDetail;
import com.example.pointserver.use.repository.UseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UseService {
    private final UseRepository useRepository;
    private final HistoryService historyService;
    private final ExpireService expireService;
    private final UseDetailService useDetailService;
    private final CancelService cancelService;
    private final CancelHistoryService cancelHistoryService;

    @Value("${policy.expire.default-period}")
    private int expirePeriod; // 기본 만료일

    /**
     * 잔액 조회
     * @param memberId 회원 아이디
     * @return member_point.balance
     */
    public Integer findBalance(long memberId) {
        return useRepository.findBalance(memberId);
    }

    /**
     * 주문번호 중복 확인
     * @param orderNo
     * @return
     */
    public boolean isDuplicateOrderNo(String orderNo) {
        List<MemberPointHistory> memberPointHistories = historyService.findHistory(orderNo);
        return !memberPointHistories.isEmpty();
    }

    /**
     * 이력 조회
     * @param memberId 회원 아이디
     * @param orderNo 주문번호
     * @return 이력
     */
    public HistoryInfo.Use findHistory(long memberId, String orderNo) {
        return historyService.findHistoryForUseCancel(memberId, orderNo);
    }

    /**
     * 사용
     * @param memberId 회원 아이디
     * @param orderNo 주문번호
     * @param point 금액
     * @param description 상세설명
     * @param expires 소멸정보
     */
    @Transactional
    public void use(
            long memberId,
            String orderNo,
            int point,
            String description,
            List<MemberPointExpire> expires
    ) {
        // 잔액 차감
        useRepository.decreaseBalance(memberId, point);

        // 이력 저장
        long historyId = historyService.insertHistory(
                memberId,
                orderNo,
                PointAction.USE.getCode(),
                point,
                description
        );

        // 소멸 계산
        int totalUsePoint = point;
        List<ExpireUpdate> expireUpdates = new ArrayList<>();
        for (MemberPointExpire expire : expires) {
            // 만료일이 지난 경우 스킵
            if (expire.getExpireDay().isBefore(LocalDate.now())) {
                continue;
            }

            // 잔액이 없는 경우 스킵
            if (expire.getExpireAmount() == 0) {
                continue;
            }

            if (totalUsePoint - expire.getExpireAmount() <= 0) {// 사용금액을 다 사용한 경우
                ExpireUpdate memberPointExpireUpdate = ExpireUpdate.builder()
                        .expireId(expire.getId())
                        .amount(totalUsePoint)
                        .orderNo(orderNo)
                        .build();
                expireUpdates.add(memberPointExpireUpdate);
                break;
            } else { // 사용금액이 남아 있는 경우
                ExpireUpdate expireUpdate = ExpireUpdate.builder()
                        .expireId(expire.getId())
                        .amount(expire.getExpireAmount())
                        .orderNo(orderNo)
                        .build();
                expireUpdates.add(expireUpdate);
            }

            totalUsePoint -= expire.getExpireAmount();
        }

        // 소멸 금액 업데이트
        for (ExpireUpdate expireUpdate : expireUpdates) {
            expireService.decreaseExpireAmount(expireUpdate.getExpireId(), expireUpdate.getAmount());
            useDetailService.insertDetail(
                    historyId,
                    expireUpdate.getExpireId(),
                    expireUpdate.getAmount()
            );
        }
    }

    /**
     * 전체취소
     * @param memberId 회원 아이디
     * @param orderNo 주문번호
     * @param amount 금액
     * @param description 상세내역
     * @param useDetailList 사용상세내역
     */
    @Transactional(rollbackFor = {Exception.class})
    public void cancelAll(
            long memberId,
            String orderNo,
            int amount,
            String description,
            List<UseDetail> useDetailList
    ) {
        // 잔액 업데이트
        useRepository.increaseBalance(memberId, amount);

        // 소멸 처리
        int calculateAmount = amount;
        for (UseDetail useDetail : useDetailList) {
            calculateAmount -= useDetail.getUseAmount();
            if (calculateAmount <= 0) { // 해당 금액만 처리
                restoreExpireAmount(memberId, orderNo, useDetail.getUseAmount() + calculateAmount, useDetail);
                break;
            } else {
                restoreExpireAmount(memberId, orderNo, useDetail.getUseAmount(), useDetail);
            }
        }

        // 취소 저장
        long cancelId = cancelService.insertCancel(
                memberId,
                orderNo,
                PointAction.USE,
                CancelType.ALL,
                amount,
                0
        );

        // 취소 이력 저장
        cancelHistoryService.insertHistory(
                cancelId,
                amount,
                description
        );
    }

    /**
     * 부분취소 (최초)
     * @param memberId 회원 아이디
     * @param orderNo 주문번호
     * @param amount 금액
     * @param cancelableAmount 앞으로 취소가능한 금액
     * @param description 취소사유
     * @param useDetailList 사용상세내역
     */
    @Transactional(rollbackFor = {Exception.class})
    public void cancelPartial(
            long memberId,
            String orderNo,
            int amount,
            int cancelableAmount,
            String description,
            List<UseDetail> useDetailList
    ) {
        // 잔액 업데이트
        useRepository.increaseBalance(memberId, amount);

        // 소멸금액 복구
        int calculateAmount = amount;
        for (UseDetail useDetail : useDetailList) {
            calculateAmount -= useDetail.getUseAmount();
            if (calculateAmount <= 0) { // 해당 금액만 처리
                restoreExpireAmount(memberId, orderNo, useDetail.getUseAmount() + calculateAmount, useDetail);
                break;
            } else {
                restoreExpireAmount(memberId, orderNo, useDetail.getUseAmount(), useDetail);
            }
        }

        // 취소 저장
        long cancelId = cancelService.insertCancel(
                memberId,
                orderNo,
                PointAction.USE,
                CancelType.PARTIAL,
                amount,
                cancelableAmount
        );

        // 취소 이력 저장
        cancelHistoryService.insertHistory(
                cancelId,
                amount,
                description
        );
    }

    /**
     * 부분취소 (수정)
     * @param cancelId 취소 아이디
     * @param memberId 회원 아이디
     * @param orderNo 주문번호
     * @param amount 취소금액
     * @param cancelableAmount 앞으로 취소가능한 금액
     * @param description 취소사유
     * @param useDetailList 사용상세내역
     */
    @Transactional(rollbackFor = {Exception.class})
    public void updateCancelPartial(
            long cancelId,
            long memberId,
            String orderNo,
            int amount,
            int cancelableAmount,
            String description,
            List<UseDetail> useDetailList
    ) {
        // 잔액 업데이트
        useRepository.increaseBalance(memberId, amount);

        // 소멸금액 복구
        int calculateAmount = amount;
        for (UseDetail useDetail : useDetailList) {
            calculateAmount -= useDetail.getUseAmount();
            if (calculateAmount <= 0) { // 해당 금액만 처리
                restoreExpireAmount(memberId, orderNo, useDetail.getUseAmount() + calculateAmount, useDetail);
                break;
            } else {
                restoreExpireAmount(memberId, orderNo, useDetail.getUseAmount(), useDetail);
            }
        }

        // 취소 저장
        cancelService.updateCancel(
                cancelId,
                amount,
                cancelableAmount
        );

        // 취소 이력 저장
        cancelHistoryService.insertHistory(
                cancelId,
                amount,
                description
        );
    }

    private void restoreExpireAmount(
            long memberId,
            String orderNo,
            int amount,
            UseDetail useDetail
    ) {
        if (useDetail.getExpireDay().isBefore(LocalDate.now())) {
            // 소멸 금액 신규 저장
            expireService.insertExpire(
                    memberId,
                    orderNo,
                    LocalDate.now().plusDays(expirePeriod),
                    amount,
                    false
            );

            // 이력 저장
            historyService.insertHistory(
                    memberId,
                    orderNo,
                    PointAction.EARN.getCode(),
                    amount,
                    "사용취소 시 적립만료일에 따른 신규 적립"
            );
        } else {
            // 소멸 금액 복구
            expireService.increaseExpireAmount(
                    useDetail.getExpireId(),
                    amount
            );
        }

        // 소멸사용금액 차감
        useDetailService.updateDetail(
                useDetail.getUsageDetailId(),
                amount
        );
    }
}
