package io.github.humphreymahlangu.votetrust.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record BallotCastRequest(
        @NotNull
        UUID contestId,

        @NotNull
        UUID contestOptionId,

        @NotBlank
        String votingCredential
) {
}
