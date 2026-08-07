package io.github.humphreymahlangu.votetrust.controller;

import io.github.humphreymahlangu.votetrust.dto.AdminBootstrapRequest;
import io.github.humphreymahlangu.votetrust.dto.AuthResponse;
import io.github.humphreymahlangu.votetrust.security.SecurityAuditMetadata;
import io.github.humphreymahlangu.votetrust.service.AdminBootstrapService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/bootstrap")
public class AdminBootstrapController {

    public static final String BOOTSTRAP_TOKEN_HEADER = "X-VoteTrust-Bootstrap-Token";

    private final AdminBootstrapService adminBootstrapService;

    public AdminBootstrapController(AdminBootstrapService adminBootstrapService) {
        this.adminBootstrapService = adminBootstrapService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Bootstrap the first admin account using the configured one-time bootstrap token")
    public AuthResponse bootstrapFirstAdmin(
            @RequestHeader(name = BOOTSTRAP_TOKEN_HEADER, required = false) String bootstrapToken,
            @Valid @RequestBody AdminBootstrapRequest request,
            HttpServletRequest servletRequest
    ) {
        return adminBootstrapService.bootstrapFirstAdmin(
                request,
                bootstrapToken,
                SecurityAuditMetadata.from(servletRequest)
        );
    }
}
