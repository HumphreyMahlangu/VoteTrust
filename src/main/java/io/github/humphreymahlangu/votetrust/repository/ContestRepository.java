package io.github.humphreymahlangu.votetrust.repository;

import io.github.humphreymahlangu.votetrust.entity.Contest;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ContestRepository extends JpaRepository<Contest, UUID> {

    @EntityGraph(attributePaths = {"election"})
    List<Contest> findByElectionIdOrderByDisplayOrderAscNameAsc(UUID electionId);

    @EntityGraph(attributePaths = {"election"})
    Optional<Contest> findByIdAndElectionId(UUID id, UUID electionId);
}
