package com.pickleball.entity;

import jakarta.persistence.*;
import lombok.*;
import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Table(name = "member_role")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
@IdClass(MemberRole.MemberRoleId.class)
public class MemberRole {

    @Id
    @Column(nullable = false, length = 20)
    private String username;

    @Id
    @Column(nullable = false, length = 20)
    private String roles;

    @Column(name = "create_date", nullable = false, updatable = false)
    private LocalDateTime createDate;

    @Column(name = "update_date")
    private LocalDateTime updateDate;

    @PrePersist
    protected void onCreate() {
        createDate = LocalDateTime.now();
    }

    @Data
    @NoArgsConstructor @AllArgsConstructor
    public static class MemberRoleId implements Serializable {
        private String username;
        private String roles;
    }
}
