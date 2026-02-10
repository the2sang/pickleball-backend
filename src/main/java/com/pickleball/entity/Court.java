package com.pickleball.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "court")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Court {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "partner_id", nullable = false)
    private Long partnerId;

    @Column(name = "court_name", nullable = false, length = 50)
    private String courtName;

    @Column(name = "personnel_number")
    private Short personnelNumber = 6;

    @Column(name = "court_level", length = 10)
    private String courtLevel;

    @Column(name = "create_date", nullable = false, updatable = false)
    private LocalDateTime createDate;

    @Column(name = "update_date")
    private LocalDateTime updateDate;

    @Column(name = "reserv_close", length = 2)
    private String reservClose = "N";

    @Column(name = "court_gugun", nullable = false, length = 2)
    private String courtGugun = "01";

    @Column(name = "game_time", length = 20)
    private String gameTime;

    @Column(name = "game_date")
    private LocalDate gameDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "partner_id", insertable = false, updatable = false)
    private Partner partner;

    @PrePersist
    protected void onCreate() {
        createDate = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updateDate = LocalDateTime.now();
    }
}
