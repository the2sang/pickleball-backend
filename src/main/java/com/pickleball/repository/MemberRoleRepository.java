package com.pickleball.repository;

import com.pickleball.entity.MemberRole;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface MemberRoleRepository extends JpaRepository<MemberRole, MemberRole.MemberRoleId> {
    List<MemberRole> findByUsername(String username);
}
