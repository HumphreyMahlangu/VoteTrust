package io.github.humphreymahlangu.votetrust.dto;

import java.time.Instant;
import java.util.UUID;

public record VotingCredentialResponse(
        UUID electionId,
        UUID contestId,
        String votingCredential,
        Instant expiresAt
) {
}
