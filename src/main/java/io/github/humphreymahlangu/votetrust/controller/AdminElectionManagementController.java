package io.github.humphreymahlangu.votetrust.controller;

import io.github.humphreymahlangu.votetrust.dto.ApiErrorResponse;
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
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Admin Election Management", description = "Admin APIs for districts, elections, contests, options, and lifecycle transitions")
@ApiResponses({
        @ApiResponse(
                responseCode = "400",
                description = "Request validation failed",
                content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
        ),
        @ApiResponse(
                responseCode = "401",
                description = "Authentication is required",
                content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
        ),
        @ApiResponse(
                responseCode = "403",
                description = "Admin role is required",
                content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
        ),
        @ApiResponse(
                responseCode = "404",
                description = "Election, contest, or voting district was not found",
                content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
        ),
        @ApiResponse(
                responseCode = "409",
                description = "Lifecycle transition or uniqueness constraint rejected the request",
                content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
        )
})
public class AdminElectionManagementController {

    private final AdminElectionManagementService adminElectionManagementService;

    public AdminElectionManagementController(AdminElectionManagementService adminElectionManagementService) {
        this.adminElectionManagementService = adminElectionManagementService;
    }

    @PostMapping("/voting-districts")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a voting district", description = "Creates a district used for voter registration and contest eligibility checks.")
    public VotingDistrictResponse createVotingDistrict(@Valid @RequestBody CreateVotingDistrictRequest request) {
        return adminElectionManagementService.createVotingDistrict(request);
    }

    @PostMapping("/elections")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create an election", description = "Creates an election in DRAFT status with registration and voting windows.")
    public ElectionResponse createElection(@Valid @RequestBody CreateElectionRequest request) {
        return adminElectionManagementService.createElection(request);
    }

    @PatchMapping("/elections/{electionId}/status")
    @Operation(summary = "Transition an election status", description = "Supports DRAFT -> REGISTRATION_OPEN -> REGISTRATION_CLOSED -> VOTING_OPEN -> COMPLETED.")
    public ElectionResponse updateElectionStatus(
            @PathVariable UUID electionId,
            @Valid @RequestBody ElectionStatusUpdateRequest request
    ) {
        return adminElectionManagementService.updateElectionStatus(electionId, request);
    }

    @PostMapping("/elections/{electionId}/contests")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a contest for an election", description = "Creates a contest with South African national, provincial, municipal PR, or municipal ward scope rules.")
    public ContestResponse createContest(
            @PathVariable UUID electionId,
            @Valid @RequestBody CreateContestRequest request
    ) {
        return adminElectionManagementService.createContest(electionId, request);
    }

    @PostMapping("/elections/{electionId}/contests/{contestId}/options")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a ballot option for a contest", description = "Adds a party, independent candidate, explicit blank ballot, or explicit spoilt ballot option to an existing contest.")
    public ContestOptionResponse createContestOption(
            @PathVariable UUID electionId,
            @PathVariable UUID contestId,
            @Valid @RequestBody CreateContestOptionRequest request
    ) {
        return adminElectionManagementService.createContestOption(electionId, contestId, request);
    }

    @PatchMapping("/elections/{electionId}/contests/{contestId}/status")
    @Operation(summary = "Transition a contest status", description = "Supports DRAFT -> OPEN -> CLOSED.")
    public ContestResponse updateContestStatus(
            @PathVariable UUID electionId,
            @PathVariable UUID contestId,
            @Valid @RequestBody ContestStatusUpdateRequest request
    ) {
        return adminElectionManagementService.updateContestStatus(electionId, contestId, request);
    }
}
