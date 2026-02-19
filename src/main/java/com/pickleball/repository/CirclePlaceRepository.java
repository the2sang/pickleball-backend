package com.pickleball.repository;

import com.pickleball.entity.CirclePlace;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CirclePlaceRepository extends JpaRepository<CirclePlace, Long> {

    List<CirclePlace> findAllByCircleIdOrderByIdDesc(Long circleId);
}
