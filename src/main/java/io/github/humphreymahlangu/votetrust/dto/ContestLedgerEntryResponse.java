package io.github.humphreymahlangu.votetrust.dto;

import java.time.Instant;
import java.util.UUID;

public record ContestLedgerEntryResponse(
        UUID id,
        long ledgerIndex,
        UUID contestOptionId,
        String previousHash,
        String currentHash,
        String nonce,
        Instant castAt
) {
}
