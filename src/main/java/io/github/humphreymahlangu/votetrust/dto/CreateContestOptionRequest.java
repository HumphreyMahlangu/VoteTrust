package io.github.humphreymahlangu.votetrust.dto;

import io.github.humphreymahlangu.votetrust.entity.ContestOptionType;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateContestOptionRequest(
        @NotBlank
        @Size(max = 160)
        String name,

        @NotNull
        ContestOptionType optionType,

        @NotNull
        @Min(1)
        @Max(9999)
        Integer displayOrder
) {
}
