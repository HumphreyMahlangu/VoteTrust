package io.github.humphreymahlangu.votetrust.controller;

import io.github.humphreymahlangu.votetrust.dto.ContestAuditResponse;
import io.github.humphreymahlangu.votetrust.dto.ContestLedgerEntryResponse;
import io.github.humphreymahlangu.votetrust.dto.ContestResultResponse;
import io.github.humphreymahlangu.votetrust.service.TallyAuditService;
import io.swagger.v3.oas.annotations.Operation;
import java.util.List;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/elections/{electionId}/contests/{contestId}")
public class ContestResultController {

    private final TallyAuditService tallyAuditService;

    public ContestResultController(TallyAuditService tallyAuditService) {
        this.tallyAuditService = tallyAuditService;
    }

    @GetMapping("/results")
    @Operation(summary = "Get final contest results")
    public ContestResultResponse getContestResults(
            @PathVariable UUID electionId,
            @PathVariable UUID contestId
    ) {
        return tallyAuditService.getContestResult(electionId, contestId);
    }

    @GetMapping("/audit")
    @Operation(summary = "Verify the contest ballot hash chain")
    public ContestAuditResponse verifyContestLedger(
            @PathVariable UUID electionId,
            @PathVariable UUID contestId
    ) {
        return tallyAuditService.verifyContestLedger(electionId, contestId);
    }

    @GetMapping("/ledger")
    @Operation(summary = "List public contest ledger entries for independent recounting")
    public List<ContestLedgerEntryResponse> listContestLedger(
            @PathVariable UUID electionId,
            @PathVariable UUID contestId
    ) {
        return tallyAuditService.listContestLedger(electionId, contestId);
    }
}
