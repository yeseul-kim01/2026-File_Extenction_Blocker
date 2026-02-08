package dev.project.web.api;

import java.util.Map;

public record ErrorResponse(
        String code,
        String message,
        String traceId,
        Map<String, String> fieldErrors
) {
    public static ErrorResponse of(String code, String message, String traceId) {
        return new ErrorResponse(code, message, traceId, null);
    }

    public static ErrorResponse of(String code, String message, String traceId, Map<String, String> fieldErrors) {
        return new ErrorResponse(code, message, traceId, fieldErrors);
    }
}
