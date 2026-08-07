package io.github.humphreymahlangu.votetrust.controller;

import io.github.humphreymahlangu.votetrust.dto.VotingDistrictResponse;
import io.github.humphreymahlangu.votetrust.service.VotingDistrictService;
import io.swagger.v3.oas.annotations.Operation;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/voting-districts")
public class VotingDistrictController {

    private final VotingDistrictService votingDistrictService;

    public VotingDistrictController(VotingDistrictService votingDistrictService) {
        this.votingDistrictService = votingDistrictService;
    }

    @GetMapping
    @Operation(summary = "List voting districts")
    public List<VotingDistrictResponse> listVotingDistricts() {
        return votingDistrictService.listVotingDistricts();
    }
}
