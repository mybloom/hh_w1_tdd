package io.hhplus.tdd.exception;

public enum ErrorCode {
    POINT_EXCEED_LIMIT("PC001", "최대 10,000 포인트까지 충전 가능합니다."),
    POINT_WITHIN_LIMIT("PC002", "최소 1부터 충전 가능합니다."),

    POINTS_INSUFFICIENT("PU001", "보유 포인트가 부족합니다."),

    USER_NOT_FOUND("PR001", "사용자를 찾을 수 없습니다.");

    private final String code;
    private final String message;

    ErrorCode(String code, String message) {
        this.code = code;
        this.message = message;
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
