package com.example.pointserver.common.entity.history;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Comment;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "member_point_expire")
@NoArgsConstructor
public class MemberPointExpire {
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
    @Comment("소멸일")
    private LocalDate expireDay;

    @Column(nullable = false)
    @Comment("소멸금액")
    private int expireAmount;

    @Column(nullable = false)
    @Comment("관리자 여부")
    private boolean isAdmin;

    @CreatedDate
    @Column(nullable = false)
    @Comment("생성일")
    private LocalDateTime createDate = LocalDateTime.now();

    @LastModifiedDate
    @Column(nullable = false)
    @Comment("수정일")
    private LocalDateTime updateDate = LocalDateTime.now();

    public boolean getAdmin() {
        return isAdmin;
    }
}
