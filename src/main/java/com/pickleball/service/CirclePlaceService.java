package com.pickleball.service;

import com.pickleball.dto.CirclePlaceDto;
import com.pickleball.entity.CirclePlace;
import com.pickleball.exception.BusinessException;
import com.pickleball.repository.CirclePlaceRepository;
import com.pickleball.repository.CircleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class CirclePlaceService {

    private final CirclePlaceRepository circlePlaceRepository;
    private final CircleRepository circleRepository;

    public CirclePlaceService(CirclePlaceRepository circlePlaceRepository, CircleRepository circleRepository) {
        this.circlePlaceRepository = circlePlaceRepository;
        this.circleRepository = circleRepository;
    }

    private Long getCircleIdByUsername(String username) {
        return circleRepository.findCircleIdByUsername(username)
                .orElseThrow(() -> new BusinessException(BusinessException.ErrorCode.MEMBER_NOT_FOUND));
    }

    public List<CirclePlaceDto.Response> getMyPlaces(String username) {
        Long circleId = getCircleIdByUsername(username);
        return circlePlaceRepository.findAllByCircleIdOrderByIdDesc(circleId).stream()
                .map(CirclePlaceDto.Response::from)
                .toList();
    }

    @Transactional
    public CirclePlaceDto.Response createPlace(String username, CirclePlaceDto.Request request) {
        Long circleId = getCircleIdByUsername(username);

        CirclePlace place = new CirclePlace();
        place.setCircleId(circleId);
        place.setPlaceName(request.getPlaceName().trim());
        place.setPersonnelNumber(request.getPersonnelNumber() == null ? (short) 30 : request.getPersonnelNumber());
        place.setPlaceType(request.getPlaceType());
        place.setGameTime(request.getGameTime());
        place.setReservClose(normalizeReservClose(request.getReservClose()));

        circlePlaceRepository.save(place);
        return CirclePlaceDto.Response.from(place);
    }

    private String normalizeReservClose(String reservClose) {
        if (reservClose == null || reservClose.isBlank()) {
            return "N";
        }
        return "Y".equalsIgnoreCase(reservClose.trim()) ? "Y" : "N";
    }
}
