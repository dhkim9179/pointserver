package com.example.pointserver.common.entity.cancel;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Comment;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "member_point_cancel")
@NoArgsConstructor
public class MemberPointCancel {
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
    @Comment("취소 동작")
    private String action;

    @Column(nullable = false)
    @Comment("취소 구분")
    private String type;

    @Column(nullable = false)
    @Comment("취소 금액")
    private int amount;

    @Column(nullable = false)
    @Comment("취소 가능한 금액")
    private int cancelableAmount;

    @CreatedDate
    @Column(nullable = false)
    @Comment("생성일")
    private LocalDateTime createDate = LocalDateTime.now();

    @LastModifiedDate
    @Column(nullable = false)
    @Comment("수정일")
    private LocalDateTime updateDate = LocalDateTime.now();
}
