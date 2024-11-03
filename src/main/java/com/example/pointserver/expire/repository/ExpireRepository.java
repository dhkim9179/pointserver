package com.example.pointserver.expire.repository;

import com.example.pointserver.common.entity.history.MemberPointExpire;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExpireRepository extends JpaRepository<MemberPointExpire, Long>, ExpireRepositoryCustom {
}
