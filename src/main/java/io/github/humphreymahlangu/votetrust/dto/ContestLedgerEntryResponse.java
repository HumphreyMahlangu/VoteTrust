package io.github.humphreymahlangu.votetrust.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import java.util.UUID;

@Schema(description = "Public anonymized ballot ledger entry")
public record ContestLedgerEntryResponse(
        @Schema(description = "Monotonic ledger position inside the contest", example = "1")
        long ledgerIndex,

        @Schema(description = "Selected contest option identifier", example = "11111111-1111-1111-1111-111111111111")
        UUID contestOptionId,

        @Schema(description = "Previous ledger entry hash, or the genesis hash for the first entry")
        String previousHash,

        @Schema(description = "SHA-256 hash for this ledger entry")
        String currentHash,

        @Schema(description = "Per-entry nonce included in the hash calculation")
        String nonce,

        @Schema(description = "Coarse recording date. Exact cast timestamp is intentionally not exposed.", example = "2026-08-07")
        LocalDate recordedDate
) {
}
