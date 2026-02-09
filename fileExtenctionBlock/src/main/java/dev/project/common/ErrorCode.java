package dev.project.common;

import org.springframework.http.HttpStatus;

public enum ErrorCode {

    // ===== Connection Code =====
    CONNECTION_CODE_NOT_FOUND(HttpStatus.NOT_FOUND, "연결 코드를 찾을 수 없습니다."),
    CONNECTION_CODE_EXPIRED(HttpStatus.GONE, "연결 코드가 만료되었습니다."),
    CONNECTION_CODE_ALREADY_USED(HttpStatus.CONFLICT, "이미 사용된 연결 코드입니다."),

    // ===== Policy =====
    POLICY_EXTENSION_INVALID(HttpStatus.BAD_REQUEST, "유효하지 않은 확장자입니다."),
    POLICY_EXTENSION_DUPLICATED(HttpStatus.CONFLICT, "이미 등록된 확장자입니다."),
    POLICY_EXTENSION_LIMIT_EXCEEDED(HttpStatus.CONFLICT, "확장자는 최대 200개까지 등록할 수 있습니다.");

    private final HttpStatus status;
    private final String message;

    ErrorCode(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }

    public HttpStatus status() {
        return status;
    }

    public String message() {
        return message;
    }
}
