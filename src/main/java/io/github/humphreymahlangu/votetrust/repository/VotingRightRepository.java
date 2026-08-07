package io.github.humphreymahlangu.votetrust.repository;

import io.github.humphreymahlangu.votetrust.entity.VotingRight;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

public interface VotingRightRepository extends JpaRepository<VotingRight, UUID> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<VotingRight> findByVoterProfileIdAndContestId(UUID voterProfileId, UUID contestId);
}
