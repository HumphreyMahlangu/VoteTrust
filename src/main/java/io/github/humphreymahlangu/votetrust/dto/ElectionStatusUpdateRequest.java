package io.github.humphreymahlangu.votetrust.dto;

import io.github.humphreymahlangu.votetrust.entity.ElectionStatus;
import jakarta.validation.constraints.NotNull;

public record ElectionStatusUpdateRequest(
        @NotNull
        ElectionStatus status
) {
}
