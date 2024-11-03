package com.example.pointserver.common.entity.cancel;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Comment;
import org.springframework.data.annotation.CreatedDate;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "member_point_cancel_history")
@NoArgsConstructor
public class MemberPointCancelHistory {
    @Id
    @Column(nullable = false)
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Comment("id")
    private long id;

    @Column(nullable = false)
    @Comment("취소 아이디")
    private long memberPointCancelId;

    @Column(nullable = false)
    @Comment("취소 금액")
    private int amount;

    @Column(nullable = false)
    @Comment("상세내역")
    private String description;

    @CreatedDate
    @Column(nullable = false)
    @Comment("생성일")
    private LocalDateTime createDate = LocalDateTime.now();
}
