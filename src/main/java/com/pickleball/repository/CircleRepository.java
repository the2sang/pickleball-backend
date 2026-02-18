package com.pickleball.repository;

import com.pickleball.entity.Circle;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface CircleRepository extends JpaRepository<Circle, Long> {

    Optional<Circle> findByAccountId(Long accountId);

    Optional<Circle> findByPartnerEmail(String partnerEmail);

    @Query("""
            SELECT c FROM Circle c
            WHERE c.partnerLevel = '1'
              AND c.businessPartner LIKE %:keyword%
            ORDER BY c.businessPartner
        """)
    Page<Circle> searchCircles(@Param("keyword") String keyword, Pageable pageable);

    @Query("""
            SELECT c FROM Circle c
            WHERE c.partnerLevel = '1'
            ORDER BY c.businessPartner
        """)
    Page<Circle> findActiveCircles(Pageable pageable);
}
