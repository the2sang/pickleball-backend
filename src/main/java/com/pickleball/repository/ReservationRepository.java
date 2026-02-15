package com.pickleball.repository;

import com.pickleball.entity.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface ReservationRepository extends JpaRepository<Reservation, Long>, ReservationRepositoryCustom {

  /**
   * 특정 코트/날짜/시간대의 유효 예약 수 조회 (비관적 잠금)
   */
  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("""
          SELECT COUNT(r) FROM Reservation r
          WHERE r.courtId = :courtId
            AND r.gameDate = :gameDate
            AND r.timeSlot = :timeSlot
            AND r.cancelYn != 'Y'
            AND r.approvalStatus = 'APPROVED'
      """)
  int countWithLock(
      @Param("courtId") Long courtId,
      @Param("gameDate") LocalDate gameDate,
      @Param("timeSlot") String timeSlot);

  /**
   * 특정 코트/날짜/시간대의 예약자 목록 조회
   */
  @Query("""
          SELECT r FROM Reservation r
          JOIN FETCH r.account a
          WHERE r.courtId = :courtId
            AND r.gameDate = :gameDate
            AND r.timeSlot = :timeSlot
            AND r.cancelYn != 'Y'
            AND r.approvalStatus = 'APPROVED'
          ORDER BY r.createDate ASC
      """)
  List<Reservation> findActivePlayers(
      @Param("courtId") Long courtId,
      @Param("gameDate") LocalDate gameDate,
      @Param("timeSlot") String timeSlot);

  /**
   * 특정 코트/날짜/시간대의 유효 예약 수 (잠금 없이)
   */
  @Query("""
          SELECT COUNT(r) FROM Reservation r
          WHERE r.courtId = :courtId
            AND r.gameDate = :gameDate
            AND r.timeSlot = :timeSlot
            AND r.cancelYn != 'Y'
            AND r.approvalStatus = 'APPROVED'
      """)
  int countActiveReservations(
      @Param("courtId") Long courtId,
      @Param("gameDate") LocalDate gameDate,
      @Param("timeSlot") String timeSlot);

  /**
   * 중복 예약 확인
   */
  Optional<Reservation> findByUsernameAndCourtIdAndGameDateAndTimeSlotAndCancelYnNotAndApprovalStatus(
      String username, Long courtId, LocalDate gameDate,
      String timeSlot, String cancelYn, String approvalStatus);

  /**
   * 내 예약 목록
   */
  @Query("""
          SELECT r FROM Reservation r
          JOIN FETCH r.court c
          WHERE r.username = :username
            AND r.cancelYn != 'Y'
            AND r.approvalStatus = 'APPROVED'
          ORDER BY r.gameDate DESC, r.timeSlot DESC
      """)
  List<Reservation> findMyReservations(@Param("username") String username);

    @Query("""
           SELECT r FROM Reservation r
           JOIN FETCH r.account a
           WHERE r.courtId = :courtId
             AND r.gameDate = :gameDate
             AND r.cancelYn != 'Y'
             AND r.reservType = '1'
             AND r.approvalStatus IN :statuses
           ORDER BY r.createDate ASC
       """)
   List<Reservation> findRentalRequests(
       @Param("courtId") Long courtId,
       @Param("gameDate") LocalDate gameDate,
       @Param("statuses") List<String> statuses);

  @Query("""
          SELECT r FROM Reservation r
          WHERE r.username = :username
            AND r.cancelYn != 'Y'
            AND r.reservType = '1'
          ORDER BY r.createDate DESC
      """)
  List<Reservation> findMyRentalRequests(@Param("username") String username);

  @Query("""
          SELECT r FROM Reservation r
          WHERE r.username = :username
            AND r.courtId = :courtId
            AND r.gameDate = :gameDate
            AND r.timeSlot = :timeSlot
            AND r.cancelYn != 'Y'
            AND r.reservType = '1'
            AND r.approvalStatus IN :statuses
      """)
  Optional<Reservation> findExistingRentalRequest(
      @Param("username") String username,
      @Param("courtId") Long courtId,
      @Param("gameDate") LocalDate gameDate,
      @Param("timeSlot") String timeSlot,
      @Param("statuses") Set<String> statuses);
}
