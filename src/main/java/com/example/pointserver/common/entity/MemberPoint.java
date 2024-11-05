package com.example.pointserver.common.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Comment;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "member_point")
@NoArgsConstructor
public class MemberPoint {
    @Id
    @Column(nullable = false)
    @Comment("회원 아이디")
    private long memberId;

    @Column(nullable = false)
    @Comment("잔액")
    private int balance;

    @CreatedDate
    @Column(nullable = false)
    @Comment("생성일")
    private LocalDateTime createDate = LocalDateTime.now();

    @LastModifiedDate
    @Column(nullable = false)
    @Comment("수정일")
    private LocalDateTime updateDate = LocalDateTime.now();
}
