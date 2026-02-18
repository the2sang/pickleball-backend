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
        // 401
        INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "INVALID_CREDENTIALS", "아이디 또는 비밀번호가 올바르지 않습니다"),
        LOGIN_HELP_SENT_FORGOT_ID(HttpStatus.UNAUTHORIZED, "LOGIN_HELP_SENT_FORGOT_ID", "로그인 실패가 5회 누적되었습니다. 등록된 메일 계정으로 아이디 정보를 발송했습니다"),
        LOGIN_HELP_SENT_PASSWORD(HttpStatus.UNAUTHORIZED, "LOGIN_HELP_SENT_PASSWORD", "비밀번호가 5회 일치하지 않습니다. 계정이 10분간 잠금 처리되었으며 등록된 메일 계정으로 안내를 발송했습니다"),

        // 423
        ACCOUNT_LOCKED(HttpStatus.LOCKED, "ACCOUNT_LOCKED", "로그인 실패가 누적되어 계정이 잠금되었습니다. 10분 후 다시 시도해주세요"),

        // 400
        COURT_FULL(HttpStatus.BAD_REQUEST, "COURT_FULL", "해당 시간대 정원이 마감되었습니다"),
        INVALID_TIME_SLOT(HttpStatus.BAD_REQUEST, "INVALID_TIME_SLOT", "유효하지 않은 시간대입니다"),
        INVALID_REQUEST_STATE(HttpStatus.BAD_REQUEST, "INVALID_REQUEST_STATE", "요청 상태가 올바르지 않습니다"),
        TERMS_REQUIRED(HttpStatus.BAD_REQUEST, "TERMS_REQUIRED", "필수 약관에 동의해야 가입할 수 있습니다"),
        PASSWORD_CONFIRM_MISMATCH(HttpStatus.BAD_REQUEST, "PASSWORD_CONFIRM_MISMATCH", "새 비밀번호와 확인 비밀번호가 일치하지 않습니다"),
        SAME_AS_OLD_PASSWORD(HttpStatus.BAD_REQUEST, "SAME_AS_OLD_PASSWORD", "새 비밀번호는 현재 비밀번호와 달라야 합니다"),

        // 403
        MEMBER_SUSPENDED(HttpStatus.FORBIDDEN, "MEMBER_SUSPENDED", "해당 사업장에서 정지된 회원입니다"),
        VOTE_REJECTED(HttpStatus.FORBIDDEN, "VOTE_REJECTED", "기존 예약자의 과반 거부 투표로 제한됩니다"),
        CANCEL_DEADLINE_PASSED(HttpStatus.FORBIDDEN, "CANCEL_DEADLINE_PASSED", "게임 개시 2시간 전 이후에는 취소할 수 없습니다"),
        GAME_TIME_PASSED(HttpStatus.FORBIDDEN, "GAME_TIME_PASSED", "이미 지난 시간대는 예약할 수 없습니다"),
        COURT_CLOSED(HttpStatus.FORBIDDEN, "COURT_CLOSED", "예약이 마감된 코트입니다"),
        NOT_OWNER(HttpStatus.FORBIDDEN, "NOT_OWNER", "본인의 예약만 취소할 수 있습니다"),
        RENTAL_NOT_ALLOWED(HttpStatus.FORBIDDEN, "RENTAL_NOT_ALLOWED", "일반 회원은 대관 시간대를 예약할 수 없습니다"),
        CURRENT_PASSWORD_INVALID(HttpStatus.FORBIDDEN, "CURRENT_PASSWORD_INVALID", "현재 비밀번호가 올바르지 않습니다"),

        // 404
        COURT_NOT_FOUND(HttpStatus.NOT_FOUND, "COURT_NOT_FOUND", "코트를 찾을 수 없습니다"),
        RESERVATION_NOT_FOUND(HttpStatus.NOT_FOUND, "RESERVATION_NOT_FOUND", "예약을 찾을 수 없습니다"),
        MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "MEMBER_NOT_FOUND", "회원을 찾을 수 없습니다"),

        // 409
        ALREADY_RESERVED(HttpStatus.CONFLICT, "ALREADY_RESERVED", "이미 해당 시간대에 예약이 있습니다"),
        USERNAME_EXISTS(HttpStatus.CONFLICT, "USERNAME_EXISTS", "이미 사용 중인 아이디입니다"),
        SCHEDULE_OVERLAP(HttpStatus.CONFLICT, "SCHEDULE_OVERLAP", "시간대가 겹치는 스케줄이 있습니다");

        private final HttpStatus status;
        private final String code;
        private final String message;

        ErrorCode(HttpStatus status, String code, String message) {
            this.status = status;
            this.code = code;
            this.message = message;
        }

        public HttpStatus getStatus() {
            return status;
        }

        public String getCode() {
            return code;
        }

        public String getMessage() {
            return message;
        }
    }
}
