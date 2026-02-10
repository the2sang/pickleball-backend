package com.pickleball.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "member")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 20)
    private String username;

    @Column(nullable = false, length = 255)
    private String password;

    @Column(name = "phone_number", nullable = false, length = 20)
    private String phoneNumber;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(name = "nic_name", length = 100)
    private String nicName;

    @Column(name = "age_range", length = 10)
    private String ageRange;

    @Column(length = 20)
    private String location;

    @Column(name = "circle_name", length = 50)
    private String circleName;

    @Column(name = "game_level", nullable = false, length = 10)
    private String gameLevel = "입문";

    @Column(name = "dupr_point", length = 10)
    private String duprPoint;

    @Column(length = 50)
    private String email;

    @Column(name = "member_level", nullable = false, length = 10)
    private String memberLevel = "정회원";

    @Column(length = 10)
    private String sex;

    @Column(name = "regist_date", nullable = false)
    private LocalDate registDate;

    @Column(name = "create_date", nullable = false, updatable = false)
    private LocalDateTime createDate;

    @Column(name = "update_date")
    private LocalDateTime updateDate;

    @PrePersist
    protected void onCreate() {
        createDate = LocalDateTime.now();
        if (registDate == null) registDate = LocalDate.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updateDate = LocalDateTime.now();
    }
}
