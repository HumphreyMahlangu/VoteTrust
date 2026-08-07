package io.github.humphreymahlangu.votetrust.entity;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Field;
import java.util.Arrays;
import org.junit.jupiter.api.Test;

class BallotAnonymitySchemaTest {

    @Test
    void ballotLedgerEntryDoesNotContainVoterLinkFields() {
        assertThat(fieldNames(BallotLedgerEntry.class))
                .doesNotContain("userAccount", "voterProfile", "votingRight", "credential");
    }

    @Test
    void anonymousCredentialDoesNotContainVoterLinkFields() {
        assertThat(fieldNames(AnonymousVotingCredential.class))
                .doesNotContain("userAccount", "voterProfile", "votingRight");
    }

    private String[] fieldNames(Class<?> type) {
        return Arrays.stream(type.getDeclaredFields())
                .map(Field::getName)
                .toArray(String[]::new);
    }
}
