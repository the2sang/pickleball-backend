package com.pickleball.dto;

import com.pickleball.entity.CirclePlace;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

import java.time.LocalDateTime;

public class CirclePlaceDto {

    public static class Request {
        @NotBlank(message = "운동장소명을 입력해주세요")
        private String placeName;

        @Min(value = 1, message = "수용인원은 1명 이상이어야 합니다")
        @Max(value = 200, message = "수용인원은 200명 이하여야 합니다")
        private Short personnelNumber;

        private String placeType;
        private String gameTime;
        private String reservClose;

        public String getPlaceName() {
            return placeName;
        }

        public void setPlaceName(String placeName) {
            this.placeName = placeName;
        }

        public Short getPersonnelNumber() {
            return personnelNumber;
        }

        public void setPersonnelNumber(Short personnelNumber) {
            this.personnelNumber = personnelNumber;
        }

        public String getPlaceType() {
            return placeType;
        }

        public void setPlaceType(String placeType) {
            this.placeType = placeType;
        }

        public String getGameTime() {
            return gameTime;
        }

        public void setGameTime(String gameTime) {
            this.gameTime = gameTime;
        }

        public String getReservClose() {
            return reservClose;
        }

        public void setReservClose(String reservClose) {
            this.reservClose = reservClose;
        }
    }

    public static class Response {
        private Long id;
        private String placeName;
        private Short personnelNumber;
        private String placeType;
        private String gameTime;
        private String reservClose;
        private LocalDateTime createDate;

        public Long getId() {
            return id;
        }

        public String getPlaceName() {
            return placeName;
        }

        public Short getPersonnelNumber() {
            return personnelNumber;
        }

        public String getPlaceType() {
            return placeType;
        }

        public String getGameTime() {
            return gameTime;
        }

        public String getReservClose() {
            return reservClose;
        }

        public LocalDateTime getCreateDate() {
            return createDate;
        }

        public static Response from(CirclePlace place) {
            Response response = new Response();
            response.id = place.getId();
            response.placeName = place.getPlaceName();
            response.personnelNumber = place.getPersonnelNumber();
            response.placeType = place.getPlaceType();
            response.gameTime = place.getGameTime();
            response.reservClose = place.getReservClose();
            response.createDate = place.getCreateDate();
            return response;
        }
    }
}
