package io.github.humphreymahlangu.votetrust.dto;

import io.github.humphreymahlangu.votetrust.entity.ContestStatus;
import jakarta.validation.constraints.NotNull;

public record ContestStatusUpdateRequest(
        @NotNull
        ContestStatus status
) {
}
