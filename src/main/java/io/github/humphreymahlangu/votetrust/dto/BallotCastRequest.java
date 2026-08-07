package io.github.humphreymahlangu.votetrust.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.UUID;

@Schema(description = "Anonymous ballot submission using a one-time voting credential")
public record BallotCastRequest(
        @NotNull
        @Schema(description = "Contest being voted in", example = "11111111-1111-1111-1111-111111111111")
        UUID contestId,

        @NotNull
        @Schema(description = "Selected contest option", example = "22222222-2222-2222-2222-222222222222")
        UUID contestOptionId,

        @NotBlank
        @Size(max = 128)
        @Schema(description = "Anonymous one-time credential issued before voting. Not a JWT.", example = "vtc_7z6T7m6pP2P9hJpQ")
        String votingCredential
) {
}
