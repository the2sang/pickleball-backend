package com.pickleball.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "reject_vote",
       uniqueConstraints = @UniqueConstraint(
           columnNames = {"court_id", "time_slot", "game_date", "target_user", "voter_user"}))
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class RejectVote {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "court_id", nullable = false)
    private Long courtId;

    @Column(name = "time_slot", nullable = false, length = 20)
    private String timeSlot;

    @Column(name = "game_date", nullable = false)
    private LocalDate gameDate;

    @Column(name = "target_user", nullable = false, length = 20)
    private String targetUser;

    @Column(name = "voter_user", nullable = false, length = 20)
    private String voterUser;

    @Column(name = "is_reject", nullable = false)
    private Boolean isReject = true;

    @Column(name = "create_date", nullable = false, updatable = false)
    private LocalDateTime createDate;

    @PrePersist
    protected void onCreate() {
        createDate = LocalDateTime.now();
    }
}
