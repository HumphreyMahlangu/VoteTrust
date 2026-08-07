package io.github.humphreymahlangu.votetrust.dto;

import io.github.humphreymahlangu.votetrust.entity.SecurityAuditEvent;
import java.time.Instant;
import java.util.UUID;

public record SecurityAuditEventResponse(
        UUID id,
        String eventType,
        String outcome,
        UUID principalUserId,
        String principalEmail,
        String clientIp,
        String userAgent,
        String detail,
        Instant occurredAt
) {

    public static SecurityAuditEventResponse from(SecurityAuditEvent event) {
        return new SecurityAuditEventResponse(
                event.getId(),
                event.getEventType().name(),
                event.getOutcome().name(),
                event.getPrincipalUserId(),
                event.getPrincipalEmail(),
                event.getClientIp(),
                event.getUserAgent(),
                event.getDetail(),
                event.getOccurredAt()
        );
    }
}
