package com.example.pointserver.cancel.history;

import com.example.pointserver.cancel.history.repository.CancelHistoryRepository;
import com.example.pointserver.common.entity.cancel.MemberPointCancelHistory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CancelHistoryService {
    private final CancelHistoryRepository cancelHistoryRepository;

    public void insertHistory(
            long cancelId,
            int amount,
            String description
    ) {
        MemberPointCancelHistory memberPointCancelHistory = new MemberPointCancelHistory();
        memberPointCancelHistory.setMemberPointCancelId(cancelId);
        memberPointCancelHistory.setAmount(amount);
        memberPointCancelHistory.setDescription(description);

        cancelHistoryRepository.save(memberPointCancelHistory);
    }
}
