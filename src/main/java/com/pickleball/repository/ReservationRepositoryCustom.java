package com.pickleball.repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface ReservationRepositoryCustom {

    List<ActivePlayerRow> findActivePlayerRows(Long courtId, LocalDate gameDate);

    List<ActivePlayerRow> findActivePlayerRows(Long courtId, LocalDate gameDate, String timeSlot);

    record ActivePlayerRow(
            String timeSlot,
            Long reservationId,
            String username,
            String name,
            String nicName,
            String gameLevel,
            String duprPoint,
            String sex,
            LocalDateTime reservedAt) {
    }
}
