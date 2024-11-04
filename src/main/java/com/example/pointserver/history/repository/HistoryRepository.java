package com.example.pointserver.history.repository;

import com.example.pointserver.common.entity.history.MemberPointHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HistoryRepository extends JpaRepository<MemberPointHistory, Long>, HistoryRepositoryCustom {
    List<MemberPointHistory> findByOrderNo(String orderNo);
}
