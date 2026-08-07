package io.github.humphreymahlangu.votetrust.service;

import io.github.humphreymahlangu.votetrust.dto.ElectionRegistrationRequest;
import io.github.humphreymahlangu.votetrust.dto.ElectionRegistrationResponse;
import io.github.humphreymahlangu.votetrust.entity.Election;
import io.github.humphreymahlangu.votetrust.entity.ElectionRegistration;
import io.github.humphreymahlangu.votetrust.entity.ElectionStatus;
import io.github.humphreymahlangu.votetrust.entity.RegistrationStatus;
import io.github.humphreymahlangu.votetrust.entity.UserAccount;
import io.github.humphreymahlangu.votetrust.entity.VoterProfile;
import io.github.humphreymahlangu.votetrust.entity.VotingDistrict;
import io.github.humphreymahlangu.votetrust.exception.DuplicateResourceException;
import io.github.humphreymahlangu.votetrust.exception.RegistrationClosedException;
import io.github.humphreymahlangu.votetrust.exception.ResourceNotFoundException;
import io.github.humphreymahlangu.votetrust.repository.ElectionRegistrationRepository;
import io.github.humphreymahlangu.votetrust.repository.ElectionRepository;
import io.github.humphreymahlangu.votetrust.repository.UserAccountRepository;
import io.github.humphreymahlangu.votetrust.repository.VoterProfileRepository;
import io.github.humphreymahlangu.votetrust.repository.VotingDistrictRepository;
import io.github.humphreymahlangu.votetrust.security.IdentityHashService;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class VoterRegistrationService {

    private final UserAccountRepository userAccountRepository;
    private final ElectionRepository electionRepository;
    private final VotingDistrictRepository votingDistrictRepository;
    private final VoterProfileRepository voterProfileRepository;
    private final ElectionRegistrationRepository electionRegistrationRepository;
    private final SouthAfricanIdNumberValidator idNumberValidator;
    private final IdentityHashService identityHashService;
    private final Clock clock;

    public VoterRegistrationService(
            UserAccountRepository userAccountRepository,
            ElectionRepository electionRepository,
            VotingDistrictRepository votingDistrictRepository,
            VoterProfileRepository voterProfileRepository,
            ElectionRegistrationRepository electionRegistrationRepository,
            SouthAfricanIdNumberValidator idNumberValidator,
            IdentityHashService identityHashService,
            Clock clock
    ) {
        this.userAccountRepository = userAccountRepository;
        this.electionRepository = electionRepository;
        this.votingDistrictRepository = votingDistrictRepository;
        this.voterProfileRepository = voterProfileRepository;
        this.electionRegistrationRepository = electionRegistrationRepository;
        this.idNumberValidator = idNumberValidator;
        this.identityHashService = identityHashService;
        this.clock = clock;
    }

    @Transactional
    public ElectionRegistrationResponse registerForElection(
            UUID userAccountId,
            UUID electionId,
            ElectionRegistrationRequest request
    ) {
        Election election = electionRepository.findById(electionId)
                .orElseThrow(() -> new ResourceNotFoundException("Election not found"));
        assertRegistrationPeriodOpen(election);

        VotingDistrict votingDistrict = votingDistrictRepository.findById(request.votingDistrictId())
                .orElseThrow(() -> new ResourceNotFoundException("Voting district not found"));
        UserAccount userAccount = userAccountRepository.findById(userAccountId)
                .orElseThrow(() -> new ResourceNotFoundException("User account not found"));

        SouthAfricanIdNumberValidator.ValidatedSouthAfricanId validatedId =
                idNumberValidator.validateForVoterRegistration(request.southAfricanIdNumber());
        String idNumberHash = identityHashService.hashSouthAfricanIdNumber(validatedId.normalizedIdNumber());

        if (voterProfileRepository.existsByIdNumberHashAndUserAccountIdNot(idNumberHash, userAccountId)) {
            throw new DuplicateResourceException("This South African ID number is already linked to another account");
        }

        VoterProfile voterProfile = voterProfileRepository.findByUserAccountId(userAccountId)
                .map(existingProfile -> assertExistingProfileMatchesId(existingProfile, idNumberHash))
                .orElseGet(() -> voterProfileRepository.save(new VoterProfile(
                        userAccount,
                        idNumberHash,
                        validatedId.dateOfBirth(),
                        votingDistrict
                )));

        if (electionRegistrationRepository.existsByVoterProfileIdAndElectionId(voterProfile.getId(), electionId)) {
            throw new DuplicateResourceException("Voter is already registered for this election");
        }

        ElectionRegistration registration = new ElectionRegistration(
                voterProfile,
                election,
                votingDistrict,
                RegistrationStatus.ACTIVE,
                Instant.now(clock)
        );

        return toResponse(electionRegistrationRepository.save(registration));
    }

    @Transactional(readOnly = true)
    public List<ElectionRegistrationResponse> listMyRegistrations(UUID userAccountId) {
        return electionRegistrationRepository.findByVoterProfileUserAccountIdOrderByRegisteredAtDesc(userAccountId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private void assertRegistrationPeriodOpen(Election election) {
        Instant now = Instant.now(clock);
        boolean withinRegistrationWindow = !now.isBefore(election.getRegistrationStartAt())
                && !now.isAfter(election.getRegistrationEndAt());

        if (election.getStatus() != ElectionStatus.REGISTRATION_OPEN || !withinRegistrationWindow) {
            throw new RegistrationClosedException("Election registration period is closed");
        }
    }

    private VoterProfile assertExistingProfileMatchesId(VoterProfile voterProfile, String idNumberHash) {
        if (!voterProfile.getIdNumberHash().equals(idNumberHash)) {
            throw new DuplicateResourceException("This account is already linked to a different South African ID number");
        }
        return voterProfile;
    }

    private ElectionRegistrationResponse toResponse(ElectionRegistration registration) {
        Election election = registration.getElection();
        VotingDistrict votingDistrict = registration.getVotingDistrict();
        return new ElectionRegistrationResponse(
                registration.getId(),
                election.getId(),
                election.getName(),
                election.getType().name(),
                registration.getStatus().name(),
                registration.getRegisteredAt(),
                votingDistrict.getId(),
                votingDistrict.getCode(),
                votingDistrict.getName(),
                votingDistrict.getProvince()
        );
    }
}
