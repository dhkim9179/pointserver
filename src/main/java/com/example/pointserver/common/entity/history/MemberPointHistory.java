package com.example.pointserver.common.entity.history;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Comment;
import org.springframework.data.annotation.CreatedDate;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "member_point_history")
@NoArgsConstructor
public class MemberPointHistory {
    @Id
    @Column(nullable = false)
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Comment("id")
    private long id;

    @Column(nullable = false)
    @Comment("회원 아이디")
    private long memberId;

    @Column(nullable = false)
    @Comment("거래번호")
    private String transactionId;

    @Column(nullable = false)
    @Comment("거래구분")
    private String transactionType;

    @Column(nullable = false)
    @Comment("포인트 동작")
    private String action;

    @Column(nullable = false)
    @Comment("포인트 금액")
    private int amount;

    @Column(nullable = false)
    @Comment("상세내역")
    private String description;

    @CreatedDate
    @Column(nullable = false)
    @Comment("생성일")
    private LocalDateTime createDate = LocalDateTime.now();
}
