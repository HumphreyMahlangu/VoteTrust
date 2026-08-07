package io.github.humphreymahlangu.votetrust.service;

import io.github.humphreymahlangu.votetrust.entity.BallotLedgerEntry;
import io.github.humphreymahlangu.votetrust.entity.Contest;
import io.github.humphreymahlangu.votetrust.entity.ContestOption;
import io.github.humphreymahlangu.votetrust.entity.LedgerState;
import io.github.humphreymahlangu.votetrust.repository.BallotLedgerEntryRepository;
import io.github.humphreymahlangu.votetrust.repository.LedgerStateRepository;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class VoteHashChainService {

    private static final int NONCE_BYTES = 24;

    private final LedgerStateRepository ledgerStateRepository;
    private final BallotLedgerEntryRepository ballotLedgerEntryRepository;
    private final SecureRandom secureRandom = new SecureRandom();

    public VoteHashChainService(
            LedgerStateRepository ledgerStateRepository,
            BallotLedgerEntryRepository ballotLedgerEntryRepository
    ) {
        this.ledgerStateRepository = ledgerStateRepository;
        this.ballotLedgerEntryRepository = ballotLedgerEntryRepository;
    }

    public BallotLedgerEntry appendVote(Contest contest, ContestOption contestOption, Instant castAt) {
        LedgerState ledgerState = ledgerStateRepository.findByContestIdForUpdate(contest.getId())
                .orElseGet(() -> ledgerStateRepository.saveAndFlush(new LedgerState(contest)));

        Long ledgerIndex = ledgerState.getNextLedgerIndex();
        String previousHash = ledgerState.getCurrentHash();
        String nonce = generateNonce();
        String currentHash = calculateHash(
                contest.getId(),
                contestOption.getId(),
                ledgerIndex,
                previousHash,
                nonce,
                castAt
        );

        BallotLedgerEntry entry = ballotLedgerEntryRepository.save(new BallotLedgerEntry(
                contest,
                contestOption,
                ledgerIndex,
                previousHash,
                currentHash,
                nonce,
                castAt
        ));
        ledgerState.advanceTo(currentHash);

        return entry;
    }

    public String calculateHash(
            UUID contestId,
            UUID contestOptionId,
            Long ledgerIndex,
            String previousHash,
            String nonce,
            Instant castAt
    ) {
        String canonicalPayload = String.join(
                "|",
                "votetrust-ledger-v1",
                contestId.toString(),
                contestOptionId.toString(),
                ledgerIndex.toString(),
                previousHash,
                nonce,
                castAt.toString()
        );

        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(canonicalPayload.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception exception) {
            throw new IllegalStateException("Could not calculate ballot ledger hash", exception);
        }
    }

    private String generateNonce() {
        byte[] bytes = new byte[NONCE_BYTES];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
