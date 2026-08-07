package io.github.humphreymahlangu.votetrust.controller;

import io.github.humphreymahlangu.votetrust.dto.ApiErrorResponse;
import io.github.humphreymahlangu.votetrust.dto.VotingDistrictResponse;
import io.github.humphreymahlangu.votetrust.service.VotingDistrictService;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/voting-districts")
@Tag(name = "Voting Districts", description = "Public South African voting district lookup")
@ApiResponses({
        @ApiResponse(
                responseCode = "500",
                description = "Unexpected server error",
                content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
        )
})
public class VotingDistrictController {

    private final VotingDistrictService votingDistrictService;

    public VotingDistrictController(VotingDistrictService votingDistrictService) {
        this.votingDistrictService = votingDistrictService;
    }

    @GetMapping
    @Operation(summary = "List voting districts", description = "Returns voting districts used during election registration and contest eligibility checks.")
    public List<VotingDistrictResponse> listVotingDistricts() {
        return votingDistrictService.listVotingDistricts();
    }
}
