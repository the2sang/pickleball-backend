package com.pickleball.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "member_suspension")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class MemberSuspension {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 20)
    private String username;

    @Column(name = "partner_id", nullable = false)
    private Long partnerId;

    @Column(name = "suspend_type", nullable = false, length = 10)
    private String suspendType;

    @Column(name = "suspend_start", nullable = false)
    private LocalDate suspendStart;

    @Column(name = "suspend_end", nullable = false)
    private LocalDate suspendEnd;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "create_date", nullable = false, updatable = false)
    private LocalDateTime createDate;

    @PrePersist
    protected void onCreate() {
        createDate = LocalDateTime.now();
    }
}
