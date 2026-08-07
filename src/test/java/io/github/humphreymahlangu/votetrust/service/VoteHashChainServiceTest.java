package io.github.humphreymahlangu.votetrust.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class VoteHashChainServiceTest {

    private final VoteHashChainService service = new VoteHashChainService(null, null);

    @Test
    void hashChangesWhenPreviousHashChanges() {
        UUID contestId = UUID.randomUUID();
        UUID optionId = UUID.randomUUID();
        Instant castAt = Instant.parse("2026-08-07T10:00:00Z");

        String firstHash = service.calculateHash(contestId, optionId, 0L, "0".repeat(64), "nonce", castAt);
        String tamperedPreviousHash = service.calculateHash(contestId, optionId, 0L, "1".repeat(64), "nonce", castAt);

        assertThat(firstHash).hasSize(64);
        assertThat(tamperedPreviousHash).hasSize(64);
        assertThat(firstHash).isNotEqualTo(tamperedPreviousHash);
    }
}
