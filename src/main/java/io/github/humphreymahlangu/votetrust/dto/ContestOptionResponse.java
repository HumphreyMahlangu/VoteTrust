package io.github.humphreymahlangu.votetrust.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;

@Schema(description = "Contest ballot option")
public record ContestOptionResponse(
        @Schema(description = "Contest option identifier", example = "11111111-1111-1111-1111-111111111111")
        UUID id,

        @Schema(description = "Party, candidate, or ballot option name", example = "Example Party")
        String name,

        @Schema(description = "Ballot option type", example = "PARTY")
        String optionType,

        @Schema(description = "Display ordering on the ballot", example = "1")
        Integer displayOrder
) {
}
