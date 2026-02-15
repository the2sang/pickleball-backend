package com.pickleball.repository;

import com.pickleball.entity.CourtSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

public interface CourtScheduleRepository extends JpaRepository<CourtSchedule, Long> {
    List<CourtSchedule> findByCourtIdAndGameDateOrderByStartTime(Long courtId, LocalDate gameDate);

    Optional<CourtSchedule> findByCourtIdAndGameDateAndStartTimeAndEndTime(
                    Long courtId,
                    LocalDate gameDate,
                    LocalTime startTime,
                    LocalTime endTime);

    @Modifying
    @Query("DELETE FROM CourtSchedule c WHERE c.courtId = :courtId AND c.gameDate = :gameDate")
    void deleteByCourtIdAndGameDate(@Param("courtId") Long courtId, @Param("gameDate") LocalDate gameDate);
}
