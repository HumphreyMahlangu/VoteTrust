package io.github.humphreymahlangu.votetrust.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.Instant;
import java.util.UUID;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(
        name = "voting_rights",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_voting_rights_profile_contest",
                columnNames = {"voter_profile_id", "contest_id"}
        )
)
public class VotingRight {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "voter_profile_id", nullable = false)
    private VoterProfile voterProfile;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "contest_id", nullable = false)
    private Contest contest;

    @Column(name = "credential_issued_at")
    private Instant credentialIssuedAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected VotingRight() {
    }

    public VotingRight(VoterProfile voterProfile, Contest contest) {
        this.voterProfile = voterProfile;
        this.contest = contest;
    }

    public UUID getId() {
        return id;
    }

    public VoterProfile getVoterProfile() {
        return voterProfile;
    }

    public Contest getContest() {
        return contest;
    }

    public Instant getCredentialIssuedAt() {
        return credentialIssuedAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public boolean hasCredentialIssued() {
        return credentialIssuedAt != null;
    }

    public void markCredentialIssued(Instant issuedAt) {
        this.credentialIssuedAt = issuedAt;
    }
}
