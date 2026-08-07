package io.github.humphreymahlangu.votetrust.service;

import io.github.humphreymahlangu.votetrust.dto.ContestAuditResponse;
import io.github.humphreymahlangu.votetrust.dto.ContestLedgerEntryResponse;
import io.github.humphreymahlangu.votetrust.dto.ContestOptionResultResponse;
import io.github.humphreymahlangu.votetrust.dto.ContestOptionTallyRow;
import io.github.humphreymahlangu.votetrust.dto.ContestResultResponse;
import io.github.humphreymahlangu.votetrust.entity.BallotLedgerEntry;
import io.github.humphreymahlangu.votetrust.entity.Contest;
import io.github.humphreymahlangu.votetrust.entity.ContestStatus;
import io.github.humphreymahlangu.votetrust.entity.Election;
import io.github.humphreymahlangu.votetrust.entity.ElectionStatus;
import io.github.humphreymahlangu.votetrust.entity.LedgerState;
import io.github.humphreymahlangu.votetrust.entity.RegistrationStatus;
import io.github.humphreymahlangu.votetrust.exception.ResourceNotFoundException;
import io.github.humphreymahlangu.votetrust.exception.ResultsUnavailableException;
import io.github.humphreymahlangu.votetrust.repository.BallotLedgerEntryRepository;
import io.github.humphreymahlangu.votetrust.repository.ContestRepository;
import io.github.humphreymahlangu.votetrust.repository.ElectionRegistrationRepository;
import io.github.humphreymahlangu.votetrust.repository.LedgerStateRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class TallyAuditService {

    private static final BigDecimal ONE_HUNDRED = BigDecimal.valueOf(100);

    private final ContestRepository contestRepository;
    private final BallotLedgerEntryRepository ballotLedgerEntryRepository;
    private final LedgerStateRepository ledgerStateRepository;
    private final ElectionRegistrationRepository electionRegistrationRepository;
    private final VoteHashChainService voteHashChainService;
    private final Clock clock;

    public TallyAuditService(
            ContestRepository contestRepository,
            BallotLedgerEntryRepository ballotLedgerEntryRepository,
            LedgerStateRepository ledgerStateRepository,
            ElectionRegistrationRepository electionRegistrationRepository,
            VoteHashChainService voteHashChainService,
            Clock clock
    ) {
        this.contestRepository = contestRepository;
        this.ballotLedgerEntryRepository = ballotLedgerEntryRepository;
        this.ledgerStateRepository = ledgerStateRepository;
        this.electionRegistrationRepository = electionRegistrationRepository;
        this.voteHashChainService = voteHashChainService;
        this.clock = clock;
    }

    public ContestResultResponse getContestResult(UUID electionId, UUID contestId) {
        Contest contest = findContest(electionId, contestId);
        assertResultsAvailable(contest);

        List<ContestOptionTallyRow> tallyRows = ballotLedgerEntryRepository.tallyContestOptions(contestId);
        long validVotes = tallyRows.stream()
                .mapToLong(ContestOptionTallyRow::voteCount)
                .sum();
        long ballotsCast = ballotLedgerEntryRepository.countByContestId(contestId);
        long registeredVoterCount = electionRegistrationRepository.countByElectionIdAndStatus(
                electionId,
                RegistrationStatus.ACTIVE
        );
        long highestVoteCount = tallyRows.stream()
                .mapToLong(ContestOptionTallyRow::voteCount)
                .max()
                .orElse(0);
        List<ContestOptionResultResponse> optionResults = tallyRows.stream()
                .map(tallyRow -> toOptionResult(tallyRow, validVotes, highestVoteCount))
                .toList();

        LedgerState ledgerState = ledgerStateRepository.findByContestId(contestId).orElse(null);
        String ledgerHeadHash = ledgerState == null ? LedgerState.GENESIS_HASH : ledgerState.getCurrentHash();

        return new ContestResultResponse(
                electionId,
                contestId,
                contest.getName(),
                contest.getType(),
                registeredVoterCount,
                ballotsCast,
                validVotes,
                ballotsCast - validVotes,
                percentage(ballotsCast, registeredVoterCount),
                ledgerHeadHash,
                Instant.now(clock),
                optionResults
        );
    }

    public ContestAuditResponse verifyContestLedger(UUID electionId, UUID contestId) {
        findClosedContest(electionId, contestId);

        List<BallotLedgerEntry> entries = ballotLedgerEntryRepository.findByContestIdOrderByLedgerIndexAsc(contestId);
        LedgerState ledgerState = ledgerStateRepository.findByContestId(contestId).orElse(null);
        List<String> violations = new ArrayList<>();

        String computedHeadHash = LedgerState.GENESIS_HASH;
        long expectedLedgerIndex = 0;
        for (BallotLedgerEntry entry : entries) {
            if (!Objects.equals(entry.getLedgerIndex(), expectedLedgerIndex)) {
                violations.add("Ledger index " + expectedLedgerIndex + " is missing or out of order");
            }
            if (!Objects.equals(entry.getPreviousHash(), computedHeadHash)) {
                violations.add("Ledger entry " + entry.getLedgerIndex() + " previous hash does not match the computed chain head");
            }

            String recalculatedHash = voteHashChainService.calculateHash(
                    contestId,
                    entry.getContestOption().getId(),
                    entry.getLedgerIndex(),
                    entry.getPreviousHash(),
                    entry.getNonce(),
                    entry.getCastAt()
            );
            if (!Objects.equals(entry.getCurrentHash(), recalculatedHash)) {
                violations.add("Ledger entry " + entry.getLedgerIndex() + " current hash does not match its payload");
            }

            computedHeadHash = recalculatedHash;
            expectedLedgerIndex++;
        }

        if (ledgerState == null && !entries.isEmpty()) {
            violations.add("Stored ledger state is missing");
        }

        String storedHeadHash = ledgerState == null ? LedgerState.GENESIS_HASH : ledgerState.getCurrentHash();
        Long storedNextLedgerIndex = ledgerState == null ? 0L : ledgerState.getNextLedgerIndex();
        if (!Objects.equals(storedHeadHash, computedHeadHash)) {
            violations.add("Stored ledger head does not match the computed chain head");
        }
        if (!Objects.equals(storedNextLedgerIndex, expectedLedgerIndex)) {
            violations.add("Stored next ledger index does not match the number of ledger entries");
        }

        return new ContestAuditResponse(
                electionId,
                contestId,
                violations.isEmpty(),
                entries.size(),
                LedgerState.GENESIS_HASH,
                computedHeadHash,
                storedHeadHash,
                storedNextLedgerIndex,
                Instant.now(clock),
                List.copyOf(violations)
        );
    }

    public List<ContestLedgerEntryResponse> listContestLedger(UUID electionId, UUID contestId) {
        findClosedContest(electionId, contestId);

        return ballotLedgerEntryRepository.findByContestIdOrderByLedgerIndexAsc(contestId).stream()
                .map(entry -> new ContestLedgerEntryResponse(
                        entry.getId(),
                        entry.getLedgerIndex(),
                        entry.getContestOption().getId(),
                        entry.getPreviousHash(),
                        entry.getCurrentHash(),
                        entry.getNonce(),
                        entry.getCastAt()
                ))
                .toList();
    }

    private Contest findClosedContest(UUID electionId, UUID contestId) {
        Contest contest = findContest(electionId, contestId);
        assertResultsAvailable(contest);
        return contest;
    }

    private Contest findContest(UUID electionId, UUID contestId) {
        return contestRepository.findByIdAndElectionId(contestId, electionId)
                .orElseThrow(() -> new ResourceNotFoundException("Contest not found"));
    }

    private void assertResultsAvailable(Contest contest) {
        Election election = contest.getElection();
        boolean votingWindowEnded = Instant.now(clock).isAfter(election.getVotingEndAt());
        if (election.getStatus() != ElectionStatus.COMPLETED
                || contest.getStatus() != ContestStatus.CLOSED
                || !votingWindowEnded) {
            throw new ResultsUnavailableException("Contest results are available only after voting has closed");
        }
    }

    private ContestOptionResultResponse toOptionResult(
            ContestOptionTallyRow tallyRow,
            long validVotes,
            long highestVoteCount
    ) {
        return new ContestOptionResultResponse(
                tallyRow.contestOptionId(),
                tallyRow.name(),
                tallyRow.optionType(),
                tallyRow.displayOrder(),
                tallyRow.voteCount(),
                percentage(tallyRow.voteCount(), validVotes),
                highestVoteCount > 0 && tallyRow.voteCount() == highestVoteCount
        );
    }

    private BigDecimal percentage(long numerator, long denominator) {
        if (denominator == 0) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        return BigDecimal.valueOf(numerator)
                .multiply(ONE_HUNDRED)
                .divide(BigDecimal.valueOf(denominator), 2, RoundingMode.HALF_UP);
    }
}
