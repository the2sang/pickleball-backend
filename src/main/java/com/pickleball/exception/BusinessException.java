package com.pickleball.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class BusinessException extends RuntimeException {

    private final ErrorCode errorCode;

    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    @Getter
    public enum ErrorCode {
        // 400
        COURT_FULL(HttpStatus.BAD_REQUEST, "COURT_FULL", "해당 시간대 정원이 마감되었습니다"),
        INVALID_TIME_SLOT(HttpStatus.BAD_REQUEST, "INVALID_TIME_SLOT", "유효하지 않은 시간대입니다"),

        // 403
        MEMBER_SUSPENDED(HttpStatus.FORBIDDEN, "MEMBER_SUSPENDED", "해당 사업장에서 정지된 회원입니다"),
        VOTE_REJECTED(HttpStatus.FORBIDDEN, "VOTE_REJECTED", "기존 예약자의 과반 거부 투표로 제한됩니다"),
        CANCEL_DEADLINE_PASSED(HttpStatus.FORBIDDEN, "CANCEL_DEADLINE_PASSED", "게임 개시 2시간 전 이후에는 취소할 수 없습니다"),
        GAME_TIME_PASSED(HttpStatus.FORBIDDEN, "GAME_TIME_PASSED", "이미 지난 시간대는 예약할 수 없습니다"),
        COURT_CLOSED(HttpStatus.FORBIDDEN, "COURT_CLOSED", "예약이 마감된 코트입니다"),
        NOT_OWNER(HttpStatus.FORBIDDEN, "NOT_OWNER", "본인의 예약만 취소할 수 있습니다"),

        // 404
        COURT_NOT_FOUND(HttpStatus.NOT_FOUND, "COURT_NOT_FOUND", "코트를 찾을 수 없습니다"),
        RESERVATION_NOT_FOUND(HttpStatus.NOT_FOUND, "RESERVATION_NOT_FOUND", "예약을 찾을 수 없습니다"),
        MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "MEMBER_NOT_FOUND", "회원을 찾을 수 없습니다"),

        // 409
        ALREADY_RESERVED(HttpStatus.CONFLICT, "ALREADY_RESERVED", "이미 해당 시간대에 예약이 있습니다"),
        USERNAME_EXISTS(HttpStatus.CONFLICT, "USERNAME_EXISTS", "이미 사용 중인 아이디입니다");

        private final HttpStatus status;
        private final String code;
        private final String message;

        ErrorCode(HttpStatus status, String code, String message) {
            this.status = status;
            this.code = code;
            this.message = message;
        }
    }
}
