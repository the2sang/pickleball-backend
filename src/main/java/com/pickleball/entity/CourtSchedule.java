package com.pickleball.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "court_schedule", uniqueConstraints = {
        @UniqueConstraint(columnNames = { "court_id", "game_date", "start_time" })
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CourtSchedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "court_id", nullable = false)
    private Long courtId;

    @Column(name = "game_date", nullable = false)
    private LocalDate gameDate;

    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    /**
     * OPEN_GAME, RENTAL
     */
    @Column(name = "schedule_type", nullable = false, length = 20)
    private String scheduleType;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "court_id", insertable = false, updatable = false)
    private Court court;
}
