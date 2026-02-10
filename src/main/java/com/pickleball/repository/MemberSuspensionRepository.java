package com.pickleball.repository;

import com.pickleball.entity.MemberSuspension;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface MemberSuspensionRepository extends JpaRepository<MemberSuspension, Long> {

    @Query("""
        SELECT ms FROM MemberSuspension ms
        WHERE ms.username = :username
          AND ms.partnerId = :partnerId
          AND ms.suspendEnd >= CURRENT_DATE
          AND ms.isActive = true
    """)
    Optional<MemberSuspension> findActiveSuspension(
            @Param("username") String username,
            @Param("partnerId") Long partnerId);
}
