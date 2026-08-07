package io.github.humphreymahlangu.votetrust.dto;

import io.github.humphreymahlangu.votetrust.entity.IdDocumentType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.util.UUID;

public record ElectionRegistrationRequest(
        @NotBlank
        @Pattern(regexp = "\\d{13}", message = "must be a 13-digit South African ID number")
        String southAfricanIdNumber,

        @NotNull
        IdDocumentType idDocumentType,

        @NotNull
        UUID votingDistrictId
) {
}
