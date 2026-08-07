package io.github.humphreymahlangu.votetrust.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record ContestAuditResponse(
        UUID electionId,
        UUID contestId,
        boolean chainValid,
        long ledgerEntryCount,
        String genesisHash,
        String computedHeadHash,
        String storedHeadHash,
        Long storedNextLedgerIndex,
        Instant verifiedAt,
        List<String> violations
) {
}
