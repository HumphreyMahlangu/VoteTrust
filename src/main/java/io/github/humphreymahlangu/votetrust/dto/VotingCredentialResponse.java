package io.github.humphreymahlangu.votetrust.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.UUID;

@Schema(description = "Anonymous one-time voting credential response")
public record VotingCredentialResponse(
        @Schema(description = "Election the credential belongs to", example = "11111111-1111-1111-1111-111111111111")
        UUID electionId,

        @Schema(description = "Contest the credential may be used for", example = "22222222-2222-2222-2222-222222222222")
        UUID contestId,

        @Schema(description = "Opaque credential. Store it temporarily and submit it with the ballot.", example = "vtc_7z6T7m6pP2P9hJpQ")
        String votingCredential,

        @Schema(description = "UTC expiry timestamp for the credential", example = "2026-08-07T20:00:00Z")
        Instant expiresAt
) {
}
