package io.github.humphreymahlangu.votetrust.repository;

import io.github.humphreymahlangu.votetrust.entity.ElectionRegistration;
import io.github.humphreymahlangu.votetrust.entity.RegistrationStatus;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ElectionRegistrationRepository extends JpaRepository<ElectionRegistration, UUID> {

    boolean existsByVoterProfileIdAndElectionId(UUID voterProfileId, UUID electionId);

    Optional<ElectionRegistration> findByVoterProfileUserAccountIdAndElectionIdAndStatus(
            UUID userAccountId,
            UUID electionId,
            RegistrationStatus status
    );

    long countByElectionIdAndStatus(UUID electionId, RegistrationStatus status);

    @EntityGraph(attributePaths = {"election", "votingDistrict"})
    List<ElectionRegistration> findByVoterProfileUserAccountIdOrderByRegisteredAtDesc(UUID userAccountId);
}
