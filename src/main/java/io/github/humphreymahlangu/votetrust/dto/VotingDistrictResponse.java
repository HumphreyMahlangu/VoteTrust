package io.github.humphreymahlangu.votetrust.dto;

import java.util.UUID;

public record VotingDistrictResponse(
        UUID id,
        String code,
        String name,
        String province,
        String municipality,
        Integer wardNumber
) {
}
