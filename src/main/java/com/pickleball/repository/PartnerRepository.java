package com.pickleball.repository;

import com.pickleball.entity.Partner;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PartnerRepository extends JpaRepository<Partner, Long> {

  Optional<Partner> findByAccountId(Long accountId);

  @Query("""
           SELECT p FROM Partner p
           WHERE p.partnerLevel = '1'
             AND (
               LOWER(p.businessPartner) LIKE LOWER(CONCAT('%', :keyword, '%'))
               OR LOWER(p.partnerAddress) LIKE LOWER(CONCAT('%', :keyword, '%'))
             )
           ORDER BY p.businessPartner
       """)
  Page<Partner> searchPartners(@Param("keyword") String keyword, Pageable pageable);

  @Query("""
           SELECT p FROM Partner p
           WHERE p.partnerLevel = '1'
           ORDER BY p.businessPartner
       """)
  Page<Partner> findActivePartners(Pageable pageable);

  @Query(value = """
          SELECT
              p.id AS id,
              p.business_partner AS businessPartner,
              p.owner AS owner,
              p.phone_number AS phoneNumber,
              p.partner_address AS partnerAddress,
              COUNT(DISTINCT c.id) AS courtCount,
              COUNT(r.id) FILTER (
                  WHERE r.cancel_yn <> 'Y'
                    AND r.approval_status = 'APPROVED'
              ) AS reservationCount,
              COUNT(r.id) FILTER (
                  WHERE :username IS NOT NULL
                    AND r.username = :username
                    AND r.cancel_yn <> 'Y'
                    AND r.approval_status = 'APPROVED'
              ) AS myReservationCount,
              CASE
                  WHEN :username IS NOT NULL AND fp.partner_id IS NOT NULL THEN TRUE
                  ELSE FALSE
              END AS favorite
          FROM partner p
          LEFT JOIN court c ON c.partner_id = p.id
          LEFT JOIN reservation r ON r.court_id = c.id
          LEFT JOIN favorite_partner fp
              ON fp.partner_id = p.id
             AND :username IS NOT NULL
             AND fp.username = :username
          WHERE p.partner_level = '1'
            AND (
              :keyword IS NULL
              OR LOWER(p.business_partner) LIKE LOWER(CONCAT('%', :keyword, '%'))
              OR LOWER(p.partner_address) LIKE LOWER(CONCAT('%', :keyword, '%'))
            )
          GROUP BY p.id, p.business_partner, p.owner, p.phone_number, p.partner_address, fp.partner_id
          ORDER BY
              CASE
                  WHEN :useMyUsageRanking = TRUE
                  THEN COUNT(r.id) FILTER (
                      WHERE :username IS NOT NULL
                        AND r.username = :username
                        AND r.cancel_yn <> 'Y'
                        AND r.approval_status = 'APPROVED'
                  )
                  ELSE COUNT(r.id) FILTER (
                      WHERE r.cancel_yn <> 'Y'
                        AND r.approval_status = 'APPROVED'
                  )
              END DESC,
              p.business_partner ASC
          LIMIT :limit
          OFFSET :offset
          """, nativeQuery = true)
  List<PartnerRankingProjection> findMainPartners(
          @Param("keyword") String keyword,
          @Param("username") String username,
          @Param("useMyUsageRanking") boolean useMyUsageRanking,
          @Param("limit") int limit,
          @Param("offset") int offset);

  @Query(value = """
          SELECT COUNT(*)
          FROM partner p
          WHERE p.partner_level = '1'
            AND (
              :keyword IS NULL
              OR LOWER(p.business_partner) LIKE LOWER(CONCAT('%', :keyword, '%'))
              OR LOWER(p.partner_address) LIKE LOWER(CONCAT('%', :keyword, '%'))
            )
          """, nativeQuery = true)
  long countMainPartners(@Param("keyword") String keyword);

  // ── 관리자용: partnerLevel 필터 없이 전체 조회 ──

  @Query("""
           SELECT p FROM Partner p
           ORDER BY p.registDate DESC, p.businessPartner
       """)
  Page<Partner> findAllPartners(Pageable pageable);

  @Query("""
            SELECT p FROM Partner p
            WHERE (
                LOWER(p.businessPartner) LIKE LOWER(CONCAT('%', :keyword, '%'))
                OR LOWER(p.partnerAddress) LIKE LOWER(CONCAT('%', :keyword, '%'))
              )
            ORDER BY p.registDate DESC, p.businessPartner
        """)
  Page<Partner> searchAllPartners(@Param("keyword") String keyword, Pageable pageable);

  interface PartnerRankingProjection {
    Long getId();

    String getBusinessPartner();

    String getOwner();

    String getPhoneNumber();

    String getPartnerAddress();

    long getCourtCount();

    long getReservationCount();

    long getMyReservationCount();

    boolean getFavorite();
  }
}
