package io.github.humphreymahlangu.votetrust.repository;

import io.github.humphreymahlangu.votetrust.entity.BallotLedgerEntry;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BallotLedgerEntryRepository extends JpaRepository<BallotLedgerEntry, UUID> {

    @EntityGraph(attributePaths = {"contest", "contestOption"})
    List<BallotLedgerEntry> findByContestIdOrderByLedgerIndexAsc(UUID contestId);
}
