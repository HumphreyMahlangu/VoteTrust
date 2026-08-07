package io.github.humphreymahlangu.votetrust.repository;

import io.github.humphreymahlangu.votetrust.entity.LedgerState;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface LedgerStateRepository extends JpaRepository<LedgerState, UUID> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select ledgerState
            from LedgerState ledgerState
            join fetch ledgerState.contest
            where ledgerState.contest.id = :contestId
            """)
    Optional<LedgerState> findByContestIdForUpdate(@Param("contestId") UUID contestId);
}
