package io.github.humphreymahlangu.votetrust.dto;

import java.time.Instant;
import java.util.Map;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Standard API error response")
public record ApiErrorResponse(
        @Schema(description = "UTC timestamp when the error response was produced", example = "2026-08-07T10:15:30Z")
        Instant timestamp,

        @Schema(description = "HTTP status code", example = "400")
        int status,

        @Schema(description = "HTTP reason phrase", example = "Bad Request")
        String error,

        @Schema(description = "Human-readable error message", example = "Request validation failed")
        String message,

        @Schema(description = "Request path that produced the error", example = "/api/v1/auth/login")
        String path,

        @Schema(description = "Field-level validation errors keyed by request field name")
        Map<String, String> fieldErrors
) {

    public static ApiErrorResponse of(int status, String error, String message, String path) {
        return new ApiErrorResponse(Instant.now(), status, error, message, path, Map.of());
    }
}
