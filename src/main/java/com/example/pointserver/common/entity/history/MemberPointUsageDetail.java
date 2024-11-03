package com.example.pointserver.common.entity.history;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Comment;
import org.springframework.data.annotation.CreatedDate;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "member_point_expire_use")
@NoArgsConstructor
public class MemberPointUsageDetail {
    @Id
    @Column(nullable = false)
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Comment("id")
    private long id;

    @Column(nullable = false)
    @Comment("소멸 아이디")
    private long memberPointExpireId;

    @Column(nullable = false)
    @Comment("이력 아이디")
    private long memberPointHistoryId;

    @Column(nullable = false)
    @Comment("차감 금액")
    private int amount;

    @CreatedDate
    @Column(nullable = false)
    @Comment("생성일")
    private LocalDateTime createDate;
}
