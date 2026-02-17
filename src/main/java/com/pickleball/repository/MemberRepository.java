package com.pickleball.repository;

import com.pickleball.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {
    Optional<Member> findByAccountId(Long accountId);
    Optional<Member> findByEmailIgnoreCase(String email);
}
