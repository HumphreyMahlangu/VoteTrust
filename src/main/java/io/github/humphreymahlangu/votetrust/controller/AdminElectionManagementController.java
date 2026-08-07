package io.github.humphreymahlangu.votetrust.controller;

import io.github.humphreymahlangu.votetrust.dto.ContestOptionResponse;
import io.github.humphreymahlangu.votetrust.dto.ContestResponse;
import io.github.humphreymahlangu.votetrust.dto.ContestStatusUpdateRequest;
import io.github.humphreymahlangu.votetrust.dto.CreateContestOptionRequest;
import io.github.humphreymahlangu.votetrust.dto.CreateContestRequest;
import io.github.humphreymahlangu.votetrust.dto.CreateElectionRequest;
import io.github.humphreymahlangu.votetrust.dto.CreateVotingDistrictRequest;
import io.github.humphreymahlangu.votetrust.dto.ElectionResponse;
import io.github.humphreymahlangu.votetrust.dto.ElectionStatusUpdateRequest;
import io.github.humphreymahlangu.votetrust.dto.VotingDistrictResponse;
import io.github.humphreymahlangu.votetrust.service.AdminElectionManagementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin")
@PreAuthorize("hasRole('ADMIN')")
@SecurityRequirement(name = "bearerAuth")
public class AdminElectionManagementController {

    private final AdminElectionManagementService adminElectionManagementService;

    public AdminElectionManagementController(AdminElectionManagementService adminElectionManagementService) {
        this.adminElectionManagementService = adminElectionManagementService;
    }

    @PostMapping("/voting-districts")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a voting district")
    public VotingDistrictResponse createVotingDistrict(@Valid @RequestBody CreateVotingDistrictRequest request) {
        return adminElectionManagementService.createVotingDistrict(request);
    }

    @PostMapping("/elections")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create an election in DRAFT status")
    public ElectionResponse createElection(@Valid @RequestBody CreateElectionRequest request) {
        return adminElectionManagementService.createElection(request);
    }

    @PatchMapping("/elections/{electionId}/status")
    @Operation(summary = "Transition an election status")
    public ElectionResponse updateElectionStatus(
            @PathVariable UUID electionId,
            @Valid @RequestBody ElectionStatusUpdateRequest request
    ) {
        return adminElectionManagementService.updateElectionStatus(electionId, request);
    }

    @PostMapping("/elections/{electionId}/contests")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a contest for an election")
    public ContestResponse createContest(
            @PathVariable UUID electionId,
            @Valid @RequestBody CreateContestRequest request
    ) {
        return adminElectionManagementService.createContest(electionId, request);
    }

    @PostMapping("/elections/{electionId}/contests/{contestId}/options")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a ballot option for a contest")
    public ContestOptionResponse createContestOption(
            @PathVariable UUID electionId,
            @PathVariable UUID contestId,
            @Valid @RequestBody CreateContestOptionRequest request
    ) {
        return adminElectionManagementService.createContestOption(electionId, contestId, request);
    }

    @PatchMapping("/elections/{electionId}/contests/{contestId}/status")
    @Operation(summary = "Transition a contest status")
    public ContestResponse updateContestStatus(
            @PathVariable UUID electionId,
            @PathVariable UUID contestId,
            @Valid @RequestBody ContestStatusUpdateRequest request
    ) {
        return adminElectionManagementService.updateContestStatus(electionId, contestId, request);
    }
}
