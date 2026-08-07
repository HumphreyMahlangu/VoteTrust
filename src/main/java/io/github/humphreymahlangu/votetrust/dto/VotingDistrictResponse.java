package io.github.humphreymahlangu.votetrust.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;

@Schema(description = "Voting district summary")
public record VotingDistrictResponse(
        @Schema(description = "Voting district identifier", example = "11111111-1111-1111-1111-111111111111")
        UUID id,

        @Schema(description = "Voting district code", example = "WC-CPT-12")
        String code,

        @Schema(description = "Voting district display name", example = "Cape Town Ward 12")
        String name,

        @Schema(description = "Province name", example = "Western Cape")
        String province,

        @Schema(description = "Municipality name", example = "City of Cape Town")
        String municipality,

        @Schema(description = "Municipal ward number", example = "12")
        Integer wardNumber
) {
}
