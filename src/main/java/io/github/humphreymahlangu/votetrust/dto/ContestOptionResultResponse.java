package io.github.humphreymahlangu.votetrust.dto;

import io.github.humphreymahlangu.votetrust.entity.ContestOptionType;
import java.math.BigDecimal;
import java.util.UUID;

public record ContestOptionResultResponse(
        UUID contestOptionId,
        String name,
        ContestOptionType optionType,
        Integer displayOrder,
        long voteCount,
        BigDecimal percentageOfValidVotes,
        boolean leading
) {
}
