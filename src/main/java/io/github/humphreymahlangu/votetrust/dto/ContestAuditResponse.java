package io.github.humphreymahlangu.votetrust.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Schema(description = "Hash-chain integrity verification result for a contest ledger")
public record ContestAuditResponse(
        @Schema(description = "Election identifier", example = "11111111-1111-1111-1111-111111111111")
        UUID electionId,

        @Schema(description = "Contest identifier", example = "22222222-2222-2222-2222-222222222222")
        UUID contestId,

        @Schema(description = "Whether every ledger entry links correctly to the next entry and stored head", example = "true")
        boolean chainValid,

        @Schema(description = "Number of public ledger entries verified", example = "250")
        long ledgerEntryCount,

        @Schema(description = "Fixed genesis hash used before the first ballot entry")
        String genesisHash,

        @Schema(description = "Head hash recomputed from the public ledger")
        String computedHeadHash,

        @Schema(description = "Head hash stored in ledger state")
        String storedHeadHash,

        @Schema(description = "Next ledger index stored in ledger state", example = "251")
        Long storedNextLedgerIndex,

        @Schema(description = "UTC timestamp when verification ran", example = "2026-08-07T20:15:30Z")
        Instant verifiedAt,

        @Schema(description = "Integrity violations found during verification")
        List<String> violations
) {
}
