package com.pickleball.repository;

import com.pickleball.entity.Court;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface CourtRepository extends JpaRepository<Court, Long> {

  List<Court> findByPartnerIdOrderByCourtName(Long partnerId);

  List<Court> findAllByPartnerIdOrderByIdDesc(Long partnerId);

  @Query("""
          SELECT c FROM Court c
          WHERE c.partnerId = :partnerId
            AND c.gameDate = :gameDate
            AND (c.reservClose IS NULL OR c.reservClose != 'Y')
          ORDER BY c.courtName, c.gameTime
      """)
  List<Court> findAvailableCourts(
      @Param("partnerId") Long partnerId,
      @Param("gameDate") LocalDate gameDate);
}
