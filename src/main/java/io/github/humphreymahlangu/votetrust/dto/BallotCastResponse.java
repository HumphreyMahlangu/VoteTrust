package io.github.humphreymahlangu.votetrust.dto;

import java.util.UUID;

public record BallotCastResponse(
        UUID contestId,
        boolean accepted,
        String message
) {
}
