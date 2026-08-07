package io.github.humphreymahlangu.votetrust.controller;

import io.github.humphreymahlangu.votetrust.dto.BallotCastRequest;
import io.github.humphreymahlangu.votetrust.dto.BallotCastResponse;
import io.github.humphreymahlangu.votetrust.service.VotingService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/ballots")
public class BallotController {

    private final VotingService votingService;

    public BallotController(VotingService votingService) {
        this.votingService = votingService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Cast an anonymous ballot using a one-time voting credential")
    public BallotCastResponse castBallot(@Valid @RequestBody BallotCastRequest request) {
        return votingService.castBallot(request);
    }
}
