package io.github.humphreymahlangu.votetrust.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.UUID;

public record BallotCastRequest(
        @NotNull
        UUID contestId,

        @NotNull
        UUID contestOptionId,

        @NotBlank
        @Size(max = 128)
        String votingCredential
) {
}
