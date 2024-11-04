package com.example.pointserver.use.detail.repository;

import com.example.pointserver.use.detail.model.UseDetail;

import java.util.List;

public interface UseDetailRepositoryCustom {
    List<UseDetail> findUsageDetail(String orderNo);
    void updateUsageDetail(long id, int amount);
}
