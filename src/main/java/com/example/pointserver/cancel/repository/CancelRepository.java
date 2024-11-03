package com.example.pointserver.cancel.repository;

import com.example.pointserver.common.entity.cancel.MemberPointCancel;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CancelRepository extends JpaRepository<MemberPointCancel, Long>, CancelRepositoryCustom {
}
