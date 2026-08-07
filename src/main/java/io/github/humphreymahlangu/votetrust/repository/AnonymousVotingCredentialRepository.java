package io.github.humphreymahlangu.votetrust.repository;

import io.github.humphreymahlangu.votetrust.entity.AnonymousVotingCredential;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AnonymousVotingCredentialRepository extends JpaRepository<AnonymousVotingCredential, UUID> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select credential
            from AnonymousVotingCredential credential
            join fetch credential.contest contest
            join fetch contest.election
            where credential.credentialHash = :credentialHash
              and contest.id = :contestId
            """)
    Optional<AnonymousVotingCredential> findByCredentialHashAndContestIdForUpdate(
            @Param("credentialHash") String credentialHash,
            @Param("contestId") UUID contestId
    );
}
