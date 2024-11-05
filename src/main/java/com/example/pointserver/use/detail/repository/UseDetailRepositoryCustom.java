package com.example.pointserver.use.detail.repository;

import com.example.pointserver.use.detail.model.UseDetail;

import java.util.List;

public interface UseDetailRepositoryCustom {
    List<UseDetail> findUsageDetail(String transactionId);
    void updateUsageDetail(long id, int amount);
    void findHistoryForTrace(String transactionId);
}
