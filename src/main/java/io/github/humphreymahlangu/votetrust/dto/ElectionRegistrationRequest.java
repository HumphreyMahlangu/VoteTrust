package io.github.humphreymahlangu.votetrust.dto;

import io.github.humphreymahlangu.votetrust.entity.IdDocumentType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.util.UUID;

@Schema(description = "Authenticated voter request for election registration")
public record ElectionRegistrationRequest(
        @NotBlank
        @Pattern(regexp = "\\d{13}", message = "must be a 13-digit South African ID number")
        @Schema(description = "13-digit South African ID number. The API stores only a peppered hash.", example = "8001015009087")
        String southAfricanIdNumber,

        @NotNull
        @Schema(description = "Identity document type supplied for registration", example = "SOUTH_AFRICAN_ID")
        IdDocumentType idDocumentType,

        @NotNull
        @Schema(description = "Voting district where the voter is registered", example = "11111111-1111-1111-1111-111111111111")
        UUID votingDistrictId
) {
}
