package io.github.humphreymahlangu.votetrust.dto;

import java.time.Instant;
import java.util.UUID;

public record BallotCastResponse(
        UUID ballotLedgerEntryId,
        UUID contestId,
        Long ledgerIndex,
        String previousHash,
        String currentHash,
        Instant castAt
) {
}
