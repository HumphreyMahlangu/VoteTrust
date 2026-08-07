package io.github.humphreymahlangu.votetrust.controller;

import io.github.humphreymahlangu.votetrust.dto.AuthResponse;
import io.github.humphreymahlangu.votetrust.dto.LoginRequest;
import io.github.humphreymahlangu.votetrust.dto.RegisterRequest;
import io.github.humphreymahlangu.votetrust.dto.UserAccountResponse;
import io.github.humphreymahlangu.votetrust.security.UserPrincipal;
import io.github.humphreymahlangu.votetrust.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a platform account")
    public AuthResponse register(@Valid @RequestBody RegisterRequest request) {
        return authService.register(request);
    }

    @PostMapping("/login")
    @Operation(summary = "Authenticate and issue a JWT")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @GetMapping("/me")
    @Operation(summary = "Return the authenticated account", security = @SecurityRequirement(name = "bearerAuth"))
    public UserAccountResponse me(@AuthenticationPrincipal UserPrincipal principal) {
        return new UserAccountResponse(principal.id(), principal.email(), principal.role(), principal.isEnabled());
    }
}
