package com.pickleball.repository;

import com.pickleball.entity.Partner;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PartnerRepository extends JpaRepository<Partner, Long> {

    @Query("""
        SELECT p FROM Partner p
        WHERE p.partnerLevel = '1'
          AND (p.businessPartner LIKE %:keyword%
               OR p.partnerAddress LIKE %:keyword%)
        ORDER BY p.businessPartner
    """)
    Page<Partner> searchPartners(@Param("keyword") String keyword, Pageable pageable);

    @Query("""
        SELECT p FROM Partner p
        WHERE p.partnerLevel = '1'
        ORDER BY p.businessPartner
    """)
    Page<Partner> findActivePartners(Pageable pageable);
}
