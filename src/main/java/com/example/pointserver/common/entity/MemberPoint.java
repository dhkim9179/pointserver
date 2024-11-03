package com.example.pointserver.common.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;
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
    private long memberId;

    @Column(nullable = false)
    private int balance;

    @CreatedDate
    @Column(nullable = false)
    private LocalDateTime createDate = LocalDateTime.now();

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updateDate = LocalDateTime.now();
}
