package com.example.pointserver.use.detail.repository;

import com.example.pointserver.common.entity.history.MemberPointUsageDetail;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UseDetailDetailRepository extends JpaRepository<MemberPointUsageDetail, Long>, UseDetailRepositoryCustom {
}
