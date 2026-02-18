package com.pickleball.repository;

import com.pickleball.entity.Partner;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;

public interface PartnerRepository extends JpaRepository<Partner, Long> {

  Optional<Partner> findByAccountId(Long accountId);
  Optional<Partner> findByPartnerEmail(String partnerEmail);

  @Query("""
          SELECT p FROM Partner p
          WHERE p.partnerLevel = '1'
            AND p.businessPartner LIKE %:keyword%
          ORDER BY p.businessPartner
      """)
  Page<Partner> searchPartners(@Param("keyword") String keyword, Pageable pageable);

  @Query("""
          SELECT p FROM Partner p
          WHERE p.partnerLevel = '1'
          ORDER BY p.businessPartner
      """)
  Page<Partner> findActivePartners(Pageable pageable);

  // ── 관리자용: partnerLevel 필터 없이 전체 조회 ──

  @Query("""
          SELECT p FROM Partner p
          ORDER BY p.registDate DESC, p.businessPartner
      """)
  Page<Partner> findAllPartners(Pageable pageable);

  @Query("""
          SELECT p FROM Partner p
          WHERE p.businessPartner LIKE %:keyword%
          ORDER BY p.registDate DESC, p.businessPartner
      """)
  Page<Partner> searchAllPartners(@Param("keyword") String keyword, Pageable pageable);
}
