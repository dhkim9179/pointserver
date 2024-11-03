package com.example.pointserver.history.repository;

import com.example.pointserver.history.model.HistoryInfo;

public interface HistoryRepositoryCustom {
    HistoryInfo.Earn findHistoryForEarnCancel(long memberId, String orderNo);
    HistoryInfo.Use findHistoryForUseCancel(long memberId, String orderNo);
}
