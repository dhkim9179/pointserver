package com.example.pointserver.use.repository;

import com.example.pointserver.common.entity.MemberPoint;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UseRepository extends JpaRepository<MemberPoint, Long>, UseRepositoryCustom {
}
