package io.github.humphreymahlangu.votetrust.dto;

import java.util.UUID;

public record ContestOptionResponse(
        UUID id,
        String name,
        String optionType,
        Integer displayOrder
) {
}
