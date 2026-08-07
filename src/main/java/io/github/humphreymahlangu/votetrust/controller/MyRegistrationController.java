package io.github.humphreymahlangu.votetrust.controller;

import io.github.humphreymahlangu.votetrust.dto.ApiErrorResponse;
import io.github.humphreymahlangu.votetrust.dto.ElectionRegistrationResponse;
import io.github.humphreymahlangu.votetrust.security.UserPrincipal;
import io.github.humphreymahlangu.votetrust.service.VoterRegistrationService;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/me/registrations")
@Tag(name = "My Registrations", description = "Authenticated voter registration lookup")
@SecurityRequirement(name = "bearerAuth")
@ApiResponses({
        @ApiResponse(
                responseCode = "401",
                description = "Authentication is required",
                content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
        )
})
public class MyRegistrationController {

    private final VoterRegistrationService voterRegistrationService;

    public MyRegistrationController(VoterRegistrationService voterRegistrationService) {
        this.voterRegistrationService = voterRegistrationService;
    }

    @GetMapping
    @Operation(
            summary = "List the authenticated voter's election registrations",
            description = "Returns election registrations linked to the bearer-token account.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public List<ElectionRegistrationResponse> listMyRegistrations(@AuthenticationPrincipal UserPrincipal principal) {
        return voterRegistrationService.listMyRegistrations(principal.id());
    }
}
