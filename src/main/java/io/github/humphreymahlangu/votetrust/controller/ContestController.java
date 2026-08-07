package io.github.humphreymahlangu.votetrust.controller;

import io.github.humphreymahlangu.votetrust.dto.ContestResponse;
import io.github.humphreymahlangu.votetrust.dto.VotingCredentialResponse;
import io.github.humphreymahlangu.votetrust.security.UserPrincipal;
import io.github.humphreymahlangu.votetrust.service.ContestService;
import io.github.humphreymahlangu.votetrust.service.VotingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/elections/{electionId}/contests")
public class ContestController {

    private final ContestService contestService;
    private final VotingService votingService;

    public ContestController(ContestService contestService, VotingService votingService) {
        this.contestService = contestService;
        this.votingService = votingService;
    }

    @GetMapping
    @Operation(summary = "List contests and ballot options for an election")
    public List<ContestResponse> listContests(@PathVariable UUID electionId) {
        return contestService.listContests(electionId);
    }

    @PostMapping("/{contestId}/credentials")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Issue an anonymous one-time voting credential", security = @SecurityRequirement(name = "bearerAuth"))
    public VotingCredentialResponse issueVotingCredential(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID electionId,
            @PathVariable UUID contestId
    ) {
        return votingService.issueVotingCredential(principal.id(), electionId, contestId);
    }
}
