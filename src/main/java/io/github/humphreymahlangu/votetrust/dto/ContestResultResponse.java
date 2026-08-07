package io.github.humphreymahlangu.votetrust.dto;

import io.github.humphreymahlangu.votetrust.entity.ContestType;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record ContestResultResponse(
        UUID electionId,
        UUID contestId,
        String contestName,
        ContestType contestType,
        long registeredVoterCount,
        long ballotsCast,
        long validVotes,
        long spoiltBallots,
        BigDecimal turnoutPercentage,
        String ledgerHeadHash,
        Instant generatedAt,
        List<ContestOptionResultResponse> options
) {
}
