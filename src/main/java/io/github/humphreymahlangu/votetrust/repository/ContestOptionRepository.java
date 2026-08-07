package io.github.humphreymahlangu.votetrust.repository;

import io.github.humphreymahlangu.votetrust.entity.ContestOption;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ContestOptionRepository extends JpaRepository<ContestOption, UUID> {

    List<ContestOption> findByContestIdOrderByDisplayOrderAscNameAsc(UUID contestId);

    @EntityGraph(attributePaths = {"contest", "contest.election"})
    Optional<ContestOption> findByIdAndContestId(UUID id, UUID contestId);
}
