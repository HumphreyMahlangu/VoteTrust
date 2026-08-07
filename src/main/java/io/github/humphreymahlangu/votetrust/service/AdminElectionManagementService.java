package io.github.humphreymahlangu.votetrust.service;

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
import io.github.humphreymahlangu.votetrust.entity.Contest;
import io.github.humphreymahlangu.votetrust.entity.ContestOption;
import io.github.humphreymahlangu.votetrust.entity.ContestStatus;
import io.github.humphreymahlangu.votetrust.entity.ContestType;
import io.github.humphreymahlangu.votetrust.entity.Election;
import io.github.humphreymahlangu.votetrust.entity.ElectionStatus;
import io.github.humphreymahlangu.votetrust.entity.ElectionType;
import io.github.humphreymahlangu.votetrust.entity.VotingDistrict;
import io.github.humphreymahlangu.votetrust.exception.DuplicateResourceException;
import io.github.humphreymahlangu.votetrust.exception.ElectionLifecycleException;
import io.github.humphreymahlangu.votetrust.exception.ResourceNotFoundException;
import io.github.humphreymahlangu.votetrust.repository.ContestOptionRepository;
import io.github.humphreymahlangu.votetrust.repository.ContestRepository;
import io.github.humphreymahlangu.votetrust.repository.ElectionRepository;
import io.github.humphreymahlangu.votetrust.repository.VotingDistrictRepository;
import java.util.Locale;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminElectionManagementService {

    private final VotingDistrictRepository votingDistrictRepository;
    private final ElectionRepository electionRepository;
    private final ContestRepository contestRepository;
    private final ContestOptionRepository contestOptionRepository;

    public AdminElectionManagementService(
            VotingDistrictRepository votingDistrictRepository,
            ElectionRepository electionRepository,
            ContestRepository contestRepository,
            ContestOptionRepository contestOptionRepository
    ) {
        this.votingDistrictRepository = votingDistrictRepository;
        this.electionRepository = electionRepository;
        this.contestRepository = contestRepository;
        this.contestOptionRepository = contestOptionRepository;
    }

    @Transactional
    public VotingDistrictResponse createVotingDistrict(CreateVotingDistrictRequest request) {
        String code = request.code().trim().toUpperCase(Locale.ROOT);
        if (votingDistrictRepository.existsByCodeIgnoreCase(code)) {
            throw new DuplicateResourceException("A voting district with this code already exists");
        }

        VotingDistrict votingDistrict = votingDistrictRepository.save(new VotingDistrict(
                code,
                request.name().trim(),
                request.province().trim(),
                request.municipality().trim(),
                request.wardNumber()
        ));
        return toVotingDistrictResponse(votingDistrict);
    }

    @Transactional
    public ElectionResponse createElection(CreateElectionRequest request) {
        validateElectionWindows(request);

        String name = request.name().trim();
        if (electionRepository.existsByNameIgnoreCase(name)) {
            throw new DuplicateResourceException("An election with this name already exists");
        }

        Election election = electionRepository.save(new Election(
                name,
                request.type(),
                request.registrationStartAt(),
                request.registrationEndAt(),
                request.votingStartAt(),
                request.votingEndAt(),
                ElectionStatus.DRAFT
        ));
        return toElectionResponse(election);
    }

    @Transactional
    public ElectionResponse updateElectionStatus(UUID electionId, ElectionStatusUpdateRequest request) {
        Election election = getElectionOrThrow(electionId);
        validateElectionTransition(election, request.status());
        election.transitionTo(request.status());
        return toElectionResponse(election);
    }

    @Transactional
    public ContestResponse createContest(UUID electionId, CreateContestRequest request) {
        Election election = getElectionOrThrow(electionId);
        ensureElectionAllowsBallotConfiguration(election);
        validateContestTypeForElection(election.getType(), request.type());

        String name = request.name().trim();
        String scopeProvince = normalizeOptional(request.scopeProvince());
        String scopeMunicipality = normalizeOptional(request.scopeMunicipality());
        Integer scopeWardNumber = request.scopeWardNumber();
        validateContestScope(request.type(), scopeProvince, scopeMunicipality, scopeWardNumber);

        if (contestRepository.existsByElectionIdAndNameIgnoreCase(electionId, name)) {
            throw new DuplicateResourceException("A contest with this name already exists for this election");
        }
        if (contestRepository.existsByElectionIdAndDisplayOrder(electionId, request.displayOrder())) {
            throw new DuplicateResourceException("A contest with this display order already exists for this election");
        }

        Contest contest = contestRepository.save(new Contest(
                election,
                name,
                request.type(),
                ContestStatus.DRAFT,
                request.displayOrder(),
                scopeProvince,
                scopeMunicipality,
                scopeWardNumber
        ));
        return toContestResponse(contest);
    }

    @Transactional
    public ContestOptionResponse createContestOption(
            UUID electionId,
            UUID contestId,
            CreateContestOptionRequest request
    ) {
        Contest contest = getContestOrThrow(electionId, contestId);
        ensureElectionAllowsBallotConfiguration(contest.getElection());
        if (contest.getStatus() != ContestStatus.DRAFT) {
            throw new ElectionLifecycleException("Contest ballot options can be changed only while the contest is DRAFT");
        }

        String name = request.name().trim();
        if (contestOptionRepository.existsByContestIdAndNameIgnoreCase(contestId, name)) {
            throw new DuplicateResourceException("A ballot option with this name already exists for this contest");
        }
        if (contestOptionRepository.existsByContestIdAndDisplayOrder(contestId, request.displayOrder())) {
            throw new DuplicateResourceException("A ballot option with this display order already exists for this contest");
        }

        ContestOption contestOption = contestOptionRepository.save(new ContestOption(
                contest,
                name,
                request.optionType(),
                request.displayOrder()
        ));
        return toOptionResponse(contestOption);
    }

    @Transactional
    public ContestResponse updateContestStatus(
            UUID electionId,
            UUID contestId,
            ContestStatusUpdateRequest request
    ) {
        Contest contest = getContestOrThrow(electionId, contestId);
        validateContestTransition(contest, request.status());
        contest.transitionTo(request.status());
        return toContestResponse(contest);
    }

    private void validateElectionWindows(CreateElectionRequest request) {
        if (!request.registrationStartAt().isBefore(request.registrationEndAt())) {
            throw new ElectionLifecycleException("Registration window must start before it ends");
        }
        if (!request.votingStartAt().isBefore(request.votingEndAt())) {
            throw new ElectionLifecycleException("Voting window must start before it ends");
        }
        if (request.registrationEndAt().isAfter(request.votingStartAt())) {
            throw new ElectionLifecycleException("Registration must end before voting starts");
        }
    }

    private void validateElectionTransition(Election election, ElectionStatus targetStatus) {
        ElectionStatus currentStatus = election.getStatus();
        if (currentStatus == targetStatus) {
            return;
        }

        boolean allowed = switch (currentStatus) {
            case DRAFT -> targetStatus == ElectionStatus.REGISTRATION_OPEN
                    || targetStatus == ElectionStatus.CANCELLED;
            case REGISTRATION_OPEN -> targetStatus == ElectionStatus.REGISTRATION_CLOSED
                    || targetStatus == ElectionStatus.CANCELLED;
            case REGISTRATION_CLOSED -> targetStatus == ElectionStatus.VOTING_OPEN
                    || targetStatus == ElectionStatus.CANCELLED;
            case VOTING_OPEN -> targetStatus == ElectionStatus.COMPLETED
                    || targetStatus == ElectionStatus.CANCELLED;
            case COMPLETED, CANCELLED -> false;
        };

        if (!allowed) {
            throw new ElectionLifecycleException(
                    "Cannot transition election from " + currentStatus + " to " + targetStatus
            );
        }

        if (targetStatus == ElectionStatus.VOTING_OPEN
                && !contestRepository.existsByElectionIdAndStatus(election.getId(), ContestStatus.OPEN)) {
            throw new ElectionLifecycleException("Election must have at least one open contest before voting opens");
        }

        if (targetStatus == ElectionStatus.COMPLETED) {
            if (contestRepository.countByElectionId(election.getId()) == 0) {
                throw new ElectionLifecycleException("Election must have contests before it can be completed");
            }
            if (contestRepository.existsByElectionIdAndStatusNot(election.getId(), ContestStatus.CLOSED)) {
                throw new ElectionLifecycleException("All contests must be closed before the election is completed");
            }
        }
    }

    private void validateContestTypeForElection(ElectionType electionType, ContestType contestType) {
        boolean allowed = switch (electionType) {
            case NATIONAL -> contestType == ContestType.NATIONAL || contestType == ContestType.PROVINCIAL;
            case PROVINCIAL -> contestType == ContestType.PROVINCIAL;
            case MUNICIPAL -> contestType == ContestType.MUNICIPAL_PR || contestType == ContestType.MUNICIPAL_WARD;
        };

        if (!allowed) {
            throw new ElectionLifecycleException(
                    "Contest type " + contestType + " is not valid for a " + electionType + " election"
            );
        }
    }

    private void validateContestScope(
            ContestType contestType,
            String scopeProvince,
            String scopeMunicipality,
            Integer scopeWardNumber
    ) {
        boolean valid = switch (contestType) {
            case NATIONAL -> scopeProvince == null && scopeMunicipality == null && scopeWardNumber == null;
            case PROVINCIAL -> scopeProvince != null && scopeMunicipality == null && scopeWardNumber == null;
            case MUNICIPAL_PR -> scopeProvince != null && scopeMunicipality != null && scopeWardNumber == null;
            case MUNICIPAL_WARD -> scopeProvince != null && scopeMunicipality != null && scopeWardNumber != null;
        };

        if (!valid) {
            throw new ElectionLifecycleException(switch (contestType) {
                case NATIONAL -> "National contests must not declare a geographic scope";
                case PROVINCIAL -> "Provincial contests require scopeProvince only";
                case MUNICIPAL_PR -> "Municipal PR contests require scopeProvince and scopeMunicipality only";
                case MUNICIPAL_WARD -> "Municipal ward contests require scopeProvince, scopeMunicipality, and scopeWardNumber";
            });
        }
    }

    private void validateContestTransition(Contest contest, ContestStatus targetStatus) {
        ContestStatus currentStatus = contest.getStatus();
        if (currentStatus == targetStatus) {
            return;
        }

        boolean allowed = switch (currentStatus) {
            case DRAFT -> targetStatus == ContestStatus.OPEN;
            case OPEN -> targetStatus == ContestStatus.CLOSED;
            case CLOSED -> false;
        };

        if (!allowed) {
            throw new ElectionLifecycleException(
                    "Cannot transition contest from " + currentStatus + " to " + targetStatus
            );
        }

        ElectionStatus electionStatus = contest.getElection().getStatus();
        if (targetStatus == ContestStatus.OPEN) {
            if (electionStatus != ElectionStatus.REGISTRATION_CLOSED && electionStatus != ElectionStatus.VOTING_OPEN) {
                throw new ElectionLifecycleException("Contest can open only after election registration is closed");
            }
            if (contestOptionRepository.countByContestId(contest.getId()) < 2) {
                throw new ElectionLifecycleException("Contest must have at least two ballot options before opening");
            }
        }

        if (electionStatus == ElectionStatus.CANCELLED || electionStatus == ElectionStatus.COMPLETED) {
            throw new ElectionLifecycleException("Contest status cannot change after the election is terminal");
        }
    }

    private void ensureElectionAllowsBallotConfiguration(Election election) {
        if (election.getStatus() == ElectionStatus.VOTING_OPEN
                || election.getStatus() == ElectionStatus.COMPLETED
                || election.getStatus() == ElectionStatus.CANCELLED) {
            throw new ElectionLifecycleException("Ballot configuration is locked after voting opens");
        }
    }

    private Election getElectionOrThrow(UUID electionId) {
        return electionRepository.findById(electionId)
                .orElseThrow(() -> new ResourceNotFoundException("Election not found"));
    }

    private String normalizeOptional(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    private Contest getContestOrThrow(UUID electionId, UUID contestId) {
        return contestRepository.findByIdAndElectionId(contestId, electionId)
                .orElseThrow(() -> new ResourceNotFoundException("Contest not found"));
    }

    private VotingDistrictResponse toVotingDistrictResponse(VotingDistrict votingDistrict) {
        return new VotingDistrictResponse(
                votingDistrict.getId(),
                votingDistrict.getCode(),
                votingDistrict.getName(),
                votingDistrict.getProvince(),
                votingDistrict.getMunicipality(),
                votingDistrict.getWardNumber()
        );
    }

    private ElectionResponse toElectionResponse(Election election) {
        return new ElectionResponse(
                election.getId(),
                election.getName(),
                election.getType().name(),
                election.getStatus().name(),
                election.getRegistrationStartAt(),
                election.getRegistrationEndAt(),
                election.getVotingStartAt(),
                election.getVotingEndAt()
        );
    }

    private ContestResponse toContestResponse(Contest contest) {
        return new ContestResponse(
                contest.getId(),
                contest.getElection().getId(),
                contest.getName(),
                contest.getType().name(),
                contest.getStatus().name(),
                contest.getDisplayOrder(),
                contest.getScopeProvince(),
                contest.getScopeMunicipality(),
                contest.getScopeWardNumber(),
                contestOptionRepository.findByContestIdOrderByDisplayOrderAscNameAsc(contest.getId())
                        .stream()
                        .map(this::toOptionResponse)
                        .toList()
        );
    }

    private ContestOptionResponse toOptionResponse(ContestOption contestOption) {
        return new ContestOptionResponse(
                contestOption.getId(),
                contestOption.getName(),
                contestOption.getOptionType().name(),
                contestOption.getDisplayOrder()
        );
    }
}
