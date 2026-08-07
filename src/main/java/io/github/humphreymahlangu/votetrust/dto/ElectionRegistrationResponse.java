package io.github.humphreymahlangu.votetrust.dto;

import java.time.Instant;
import java.util.UUID;

public record ElectionRegistrationResponse(
        UUID id,
        UUID electionId,
        String electionName,
        String electionType,
        String status,
        Instant registeredAt,
        UUID votingDistrictId,
        String votingDistrictCode,
        String votingDistrictName,
        String province
) {
}
