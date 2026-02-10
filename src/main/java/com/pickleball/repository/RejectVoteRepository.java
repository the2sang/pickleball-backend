package com.pickleball.repository;

import com.pickleball.entity.RejectVote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;

public interface RejectVoteRepository extends JpaRepository<RejectVote, Long> {

    @Query("""
        SELECT COUNT(v) FROM RejectVote v
        WHERE v.courtId = :courtId
          AND v.timeSlot = :timeSlot
          AND v.gameDate = :gameDate
          AND v.targetUser = :targetUser
          AND v.isReject = true
    """)
    int countRejects(
            @Param("courtId") Long courtId,
            @Param("timeSlot") String timeSlot,
            @Param("gameDate") LocalDate gameDate,
            @Param("targetUser") String targetUser);

    boolean existsByCourtIdAndTimeSlotAndGameDateAndTargetUserAndVoterUser(
            Long courtId, String timeSlot, LocalDate gameDate,
            String targetUser, String voterUser);
}
