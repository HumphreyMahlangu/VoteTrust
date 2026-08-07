package io.github.humphreymahlangu.votetrust.dto;

import java.util.List;
import java.util.UUID;

public record ContestResponse(
        UUID id,
        UUID electionId,
        String name,
        String type,
        String status,
        Integer displayOrder,
        List<ContestOptionResponse> options
) {
}
