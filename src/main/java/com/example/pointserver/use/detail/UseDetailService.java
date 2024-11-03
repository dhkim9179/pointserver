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
     * @param orderNo 주문번호
     * @return 사용 상세 내역
     */
    public List<UseDetail> findDetail(String orderNo) {
        return useDetailRepository.findExpireUses(orderNo);
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
}
