package dev.project.web.api;

import java.time.Instant;
import java.util.Map;

public record ErrorResponse(
        String code,
        String message,
        String traceId,
        Instant timestamp,
        Map<String, String> fieldErrors
) {

    public static ErrorResponse of(String code, String message, String traceId) {
        return new ErrorResponse(
                code,
                message,
                traceId,
                Instant.now(),
                null
        );
    }

    public static ErrorResponse of(
            String code,
            String message,
            String traceId,
            Map<String, String> fieldErrors
    ) {
        return new ErrorResponse(
                code,
                message,
                traceId,
                Instant.now(),
                fieldErrors
        );
    }
}
