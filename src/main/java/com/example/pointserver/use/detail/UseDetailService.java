package com.example.pointserver.use.detail;

import com.example.pointserver.common.entity.history.MemberPointUsageDetail;
import com.example.pointserver.use.detail.model.UseDetail;
import com.example.pointserver.use.detail.repository.UseDetailDetailRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UseDetailService {
    private final UseDetailDetailRepository useDetailRepository;

    /**
     * 사용 상세 내역 조회
     * @param transactionId 거래번호
     * @return 사용 상세 내역
     */
    public List<UseDetail> findDetail(String transactionId) {
        return useDetailRepository.findUsageDetail(transactionId);
    }

    /**
     * 사용 상세 내역 저장
     * @param historyId 이력 아이디
     * @param expireId 소멸 아이디
     * @param amount 금액
     */
    public void insertDetail(long historyId, long expireId, int amount) {
        MemberPointUsageDetail memberPointUsageDetail = new MemberPointUsageDetail();
        memberPointUsageDetail.setMemberPointHistoryId(historyId);
        memberPointUsageDetail.setMemberPointExpireId(expireId);
        memberPointUsageDetail.setAmount(amount);

        useDetailRepository.save(memberPointUsageDetail);
    }

    /**
     * 사용취소 시 차감
     * @param id
     * @param amount
     */
    public void updateDetail(long id, int amount) {
        useDetailRepository.updateUsageDetail(id, amount);
    }
}
