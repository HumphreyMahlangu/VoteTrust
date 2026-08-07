package io.github.humphreymahlangu.votetrust.controller;

import io.github.humphreymahlangu.votetrust.dto.ElectionRegistrationRequest;
import io.github.humphreymahlangu.votetrust.dto.ElectionRegistrationResponse;
import io.github.humphreymahlangu.votetrust.dto.ElectionResponse;
import io.github.humphreymahlangu.votetrust.security.UserPrincipal;
import io.github.humphreymahlangu.votetrust.service.ElectionService;
import io.github.humphreymahlangu.votetrust.service.VoterRegistrationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/elections")
public class ElectionController {

    private final ElectionService electionService;
    private final VoterRegistrationService voterRegistrationService;

    public ElectionController(ElectionService electionService, VoterRegistrationService voterRegistrationService) {
        this.electionService = electionService;
        this.voterRegistrationService = voterRegistrationService;
    }

    @GetMapping
    @Operation(summary = "List elections")
    public List<ElectionResponse> listElections() {
        return electionService.listElections();
    }

    @GetMapping("/{electionId}")
    @Operation(summary = "Get election details")
    public ElectionResponse getElection(@PathVariable UUID electionId) {
        return electionService.getElection(electionId);
    }

    @PostMapping("/{electionId}/registrations")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Register authenticated voter for an election", security = @SecurityRequirement(name = "bearerAuth"))
    public ElectionRegistrationResponse registerForElection(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID electionId,
            @Valid @RequestBody ElectionRegistrationRequest request
    ) {
        return voterRegistrationService.registerForElection(principal.id(), electionId, request);
    }
}
