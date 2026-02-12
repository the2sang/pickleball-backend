package com.pickleball.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "reservation")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "court_id", nullable = false)
    private Long courtId;

    @Column(nullable = false, length = 20)
    private String username;

    @Builder.Default
    @Column(name = "cancel_yn", length = 1)
    private String cancelYn = "N";

    @Builder.Default
    @Column(name = "time_name", nullable = false, length = 50)
    private String timeName = "일반예약";

    @Column(name = "game_date", nullable = false)
    private LocalDate gameDate;

    @Column(name = "time_slot", nullable = false, length = 20)
    private String timeSlot;

    @Builder.Default
    @Column(name = "reserv_type", length = 10)
    private String reservType = "0";

    @Column(name = "create_date", nullable = false, updatable = false)
    private LocalDateTime createDate;

    @Column(name = "update_date")
    private LocalDateTime updateDate;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "court_id", insertable = false, updatable = false)
    private Court court;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "username", referencedColumnName = "username", insertable = false, updatable = false)
    private Account account;

    @PrePersist
    protected void onCreate() {
        createDate = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updateDate = LocalDateTime.now();
    }
}
