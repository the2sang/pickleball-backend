package com.pickleball.repository;

import com.pickleball.entity.FavoritePartner;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FavoritePartnerRepository extends JpaRepository<FavoritePartner, Long> {

    long countByUsername(String username);

    boolean existsByUsernameAndPartnerId(String username, Long partnerId);

    void deleteByUsernameAndPartnerId(String username, Long partnerId);
}
