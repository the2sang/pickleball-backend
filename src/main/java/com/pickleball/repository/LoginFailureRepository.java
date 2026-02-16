package com.pickleball.repository;

import com.pickleball.entity.LoginFailure;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LoginFailureRepository extends JpaRepository<LoginFailure, String> {
}
