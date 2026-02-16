package com.pickleball.dto;

import lombok.*;

public class PartnerDto {

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class Response {
        private Long id;
        private String businessPartner;
        private String owner;
        private String phoneNumber;
        private String partnerAddress;
        private long courtCount;
        private long reservationCount;
        private long myReservationCount;
        private boolean favorite;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class PageResponse<T> {
        private java.util.List<T> data;
        private long total;
        private int page;
        private int size;
    }
}
