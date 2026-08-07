package io.github.humphreymahlangu.votetrust.dto;

import java.time.LocalDate;
import java.util.UUID;

public record ContestLedgerEntryResponse(
        long ledgerIndex,
        UUID contestOptionId,
        String previousHash,
        String currentHash,
        String nonce,
        LocalDate recordedDate
) {
}
