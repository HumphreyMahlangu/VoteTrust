package io.github.humphreymahlangu.votetrust.dto;

import java.time.Instant;
import java.util.UUID;

public record ElectionResponse(
        UUID id,
        String name,
        String type,
        String status,
        Instant registrationStartAt,
        Instant registrationEndAt,
        Instant votingStartAt,
        Instant votingEndAt
) {
}
