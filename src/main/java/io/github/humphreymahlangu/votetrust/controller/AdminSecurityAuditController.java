package io.github.humphreymahlangu.votetrust.controller;

import io.github.humphreymahlangu.votetrust.dto.SecurityAuditEventResponse;
import io.github.humphreymahlangu.votetrust.service.SecurityAuditService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/security-audit-events")
public class AdminSecurityAuditController {

    private final SecurityAuditService securityAuditService;

    public AdminSecurityAuditController(SecurityAuditService securityAuditService) {
        this.securityAuditService = securityAuditService;
    }

    @GetMapping
    @Operation(
            summary = "Return recent security audit events",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public List<SecurityAuditEventResponse> listRecentSecurityAuditEvents(
            @RequestParam(defaultValue = "50") int limit
    ) {
        return securityAuditService.listLatest(limit);
    }
}
