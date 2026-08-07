package io.github.humphreymahlangu.votetrust.dto;

import io.github.humphreymahlangu.votetrust.entity.ElectionType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.Instant;

public record CreateElectionRequest(
        @NotBlank
        @Size(max = 160)
        String name,

        @NotNull
        ElectionType type,

        @NotNull
        Instant registrationStartAt,

        @NotNull
        Instant registrationEndAt,

        @NotNull
        Instant votingStartAt,

        @NotNull
        Instant votingEndAt
) {
}
