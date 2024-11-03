package com.example.pointserver.earn.repository;

import com.example.pointserver.common.entity.MemberPoint;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EarnRepository extends JpaRepository<MemberPoint, Long>, EarnRepositoryCustom {
}
