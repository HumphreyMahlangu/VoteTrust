package io.github.humphreymahlangu.votetrust.dto;

import java.util.UUID;

public record UserAccountResponse(
        UUID id,
        String email,
        String role,
        boolean enabled
) {
}
