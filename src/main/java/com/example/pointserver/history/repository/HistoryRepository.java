package com.example.pointserver.history.repository;

import com.example.pointserver.common.entity.history.MemberPointHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HistoryRepository extends JpaRepository<MemberPointHistory, Long>, HistoryRepositoryCustom {
}
