package io.github.humphreymahlangu.votetrust.service;

import io.github.humphreymahlangu.votetrust.dto.BallotCastRequest;
import io.github.humphreymahlangu.votetrust.dto.BallotCastResponse;
import io.github.humphreymahlangu.votetrust.dto.VotingCredentialResponse;
import io.github.humphreymahlangu.votetrust.entity.AnonymousVotingCredential;
import io.github.humphreymahlangu.votetrust.entity.BallotLedgerEntry;
import io.github.humphreymahlangu.votetrust.entity.Contest;
import io.github.humphreymahlangu.votetrust.entity.ContestOption;
import io.github.humphreymahlangu.votetrust.entity.ContestStatus;
import io.github.humphreymahlangu.votetrust.entity.Election;
import io.github.humphreymahlangu.votetrust.entity.ElectionRegistration;
import io.github.humphreymahlangu.votetrust.entity.ElectionStatus;
import io.github.humphreymahlangu.votetrust.entity.RegistrationStatus;
import io.github.humphreymahlangu.votetrust.entity.VoterProfile;
import io.github.humphreymahlangu.votetrust.entity.VotingDistrict;
import io.github.humphreymahlangu.votetrust.entity.VotingRight;
import io.github.humphreymahlangu.votetrust.exception.DuplicateResourceException;
import io.github.humphreymahlangu.votetrust.exception.EligibilityException;
import io.github.humphreymahlangu.votetrust.exception.InvalidVotingCredentialException;
import io.github.humphreymahlangu.votetrust.exception.ResourceNotFoundException;
import io.github.humphreymahlangu.votetrust.exception.VotingClosedException;
import io.github.humphreymahlangu.votetrust.repository.AnonymousVotingCredentialRepository;
import io.github.humphreymahlangu.votetrust.repository.ContestOptionRepository;
import io.github.humphreymahlangu.votetrust.repository.ContestRepository;
import io.github.humphreymahlangu.votetrust.repository.ElectionRegistrationRepository;
import io.github.humphreymahlangu.votetrust.repository.VotingRightRepository;
import io.github.humphreymahlangu.votetrust.security.VoteCredentialService;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class VotingService {

    private final ContestRepository contestRepository;
    private final ContestOptionRepository contestOptionRepository;
    private final ElectionRegistrationRepository electionRegistrationRepository;
    private final VotingRightRepository votingRightRepository;
    private final AnonymousVotingCredentialRepository anonymousVotingCredentialRepository;
    private final VoteCredentialService voteCredentialService;
    private final VoteHashChainService voteHashChainService;
    private final Clock clock;

    public VotingService(
            ContestRepository contestRepository,
            ContestOptionRepository contestOptionRepository,
            ElectionRegistrationRepository electionRegistrationRepository,
            VotingRightRepository votingRightRepository,
            AnonymousVotingCredentialRepository anonymousVotingCredentialRepository,
            VoteCredentialService voteCredentialService,
            VoteHashChainService voteHashChainService,
            Clock clock
    ) {
        this.contestRepository = contestRepository;
        this.contestOptionRepository = contestOptionRepository;
        this.electionRegistrationRepository = electionRegistrationRepository;
        this.votingRightRepository = votingRightRepository;
        this.anonymousVotingCredentialRepository = anonymousVotingCredentialRepository;
        this.voteCredentialService = voteCredentialService;
        this.voteHashChainService = voteHashChainService;
        this.clock = clock;
    }

    @Transactional
    public VotingCredentialResponse issueVotingCredential(UUID userAccountId, UUID electionId, UUID contestId) {
        Contest contest = contestRepository.findByIdAndElectionId(contestId, electionId)
                .orElseThrow(() -> new ResourceNotFoundException("Contest not found"));
        Election election = contest.getElection();
        assertVotingOpen(contest, election);

        ElectionRegistration registration = electionRegistrationRepository
                .findByVoterProfileUserAccountIdAndElectionIdAndStatus(
                        userAccountId,
                        electionId,
                        RegistrationStatus.ACTIVE
                )
                .orElseThrow(() -> new EligibilityException("Voter is not registered for this election"));
        VoterProfile voterProfile = registration.getVoterProfile();
        assertOldEnoughToVote(voterProfile, election);
        assertVotingDistrictEligible(contest, registration.getVotingDistrict());

        VotingRight votingRight = votingRightRepository
                .findByVoterProfileIdAndContestId(voterProfile.getId(), contestId)
                .orElseGet(() -> votingRightRepository.save(new VotingRight(voterProfile, contest)));

        if (votingRight.hasCredentialIssued()) {
            throw new DuplicateResourceException("A voting credential has already been issued for this contest");
        }

        Instant now = Instant.now(clock);
        String rawCredential = voteCredentialService.generateRawCredential();
        String credentialHash = voteCredentialService.hashCredential(rawCredential);
        anonymousVotingCredentialRepository.save(new AnonymousVotingCredential(
                contest,
                credentialHash,
                now,
                election.getVotingEndAt()
        ));
        votingRight.markCredentialIssued(now);

        return new VotingCredentialResponse(election.getId(), contest.getId(), rawCredential, election.getVotingEndAt());
    }

    @Transactional
    public BallotCastResponse castBallot(BallotCastRequest request) {
        ContestOption contestOption = contestOptionRepository
                .findByIdAndContestId(request.contestOptionId(), request.contestId())
                .orElseThrow(() -> new ResourceNotFoundException("Contest option not found"));
        Contest contest = contestOption.getContest();
        Election election = contest.getElection();
        assertVotingOpen(contest, election);

        String credentialHash = voteCredentialService.hashCredential(request.votingCredential().trim());
        AnonymousVotingCredential credential = anonymousVotingCredentialRepository
                .findByCredentialHashAndContestIdForUpdate(credentialHash, request.contestId())
                .orElseThrow(InvalidVotingCredentialException::new);

        Instant now = Instant.now(clock);
        if (credential.isUsed()) {
            throw new DuplicateResourceException("Voting credential has already been used");
        }
        if (credential.getExpiresAt().isBefore(now)) {
            throw new InvalidVotingCredentialException();
        }

        credential.markUsed(now);
        BallotLedgerEntry ballotLedgerEntry = voteHashChainService.appendVote(contest, contestOption, now);

        return new BallotCastResponse(
                ballotLedgerEntry.getId(),
                contest.getId(),
                ballotLedgerEntry.getLedgerIndex(),
                ballotLedgerEntry.getPreviousHash(),
                ballotLedgerEntry.getCurrentHash(),
                ballotLedgerEntry.getCastAt()
        );
    }

    private void assertVotingOpen(Contest contest, Election election) {
        Instant now = Instant.now(clock);
        boolean withinVotingWindow = !now.isBefore(election.getVotingStartAt())
                && !now.isAfter(election.getVotingEndAt());

        if (election.getStatus() != ElectionStatus.VOTING_OPEN
                || contest.getStatus() != ContestStatus.OPEN
                || !withinVotingWindow) {
            throw new VotingClosedException("Election voting period is closed");
        }
    }

    private void assertOldEnoughToVote(VoterProfile voterProfile, Election election) {
        LocalDate votingDate = LocalDate.ofInstant(election.getVotingStartAt(), ZoneOffset.UTC);
        if (voterProfile.getDateOfBirth().plusYears(18).isAfter(votingDate)) {
            throw new EligibilityException("Voters must be at least 18 years old to vote");
        }
    }

    private void assertVotingDistrictEligible(Contest contest, VotingDistrict votingDistrict) {
        boolean eligible = switch (contest.getType()) {
            case NATIONAL -> true;
            case PROVINCIAL -> sameText(contest.getScopeProvince(), votingDistrict.getProvince());
            case MUNICIPAL_PR -> sameText(contest.getScopeProvince(), votingDistrict.getProvince())
                    && sameText(contest.getScopeMunicipality(), votingDistrict.getMunicipality());
            case MUNICIPAL_WARD -> sameText(contest.getScopeProvince(), votingDistrict.getProvince())
                    && sameText(contest.getScopeMunicipality(), votingDistrict.getMunicipality())
                    && contest.getScopeWardNumber() != null
                    && contest.getScopeWardNumber().equals(votingDistrict.getWardNumber());
        };

        if (!eligible) {
            throw new EligibilityException("Voter is not eligible for this contest based on registered voting district");
        }
    }

    private boolean sameText(String expected, String actual) {
        return expected != null && actual != null && expected.trim().equalsIgnoreCase(actual.trim());
    }
}
