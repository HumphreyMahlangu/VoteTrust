package io.github.humphreymahlangu.votetrust.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CreateVotingDistrictRequest(
        @NotBlank
        @Size(max = 32)
        @Pattern(regexp = "^[A-Za-z0-9-]+$", message = "must contain only letters, digits, and hyphens")
        String code,

        @NotBlank
        @Size(max = 160)
        String name,

        @NotBlank
        @Size(max = 80)
        String province,

        @NotBlank
        @Size(max = 160)
        String municipality,

        @NotNull
        @Min(1)
        @Max(9999)
        Integer wardNumber
) {
}
