package io.github.humphreymahlangu.votetrust.repository;

import io.github.humphreymahlangu.votetrust.entity.ElectionRegistration;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ElectionRegistrationRepository extends JpaRepository<ElectionRegistration, UUID> {

    boolean existsByVoterProfileIdAndElectionId(UUID voterProfileId, UUID electionId);

    @EntityGraph(attributePaths = {"election", "votingDistrict"})
    List<ElectionRegistration> findByVoterProfileUserAccountIdOrderByRegisteredAtDesc(UUID userAccountId);
}
