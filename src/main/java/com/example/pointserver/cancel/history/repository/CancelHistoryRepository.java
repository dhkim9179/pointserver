package com.example.pointserver.cancel.history.repository;

import com.example.pointserver.common.entity.cancel.MemberPointCancelHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CancelHistoryRepository extends JpaRepository<MemberPointCancelHistory, Long>, CancelHistoryRepositoryCustom {
}
