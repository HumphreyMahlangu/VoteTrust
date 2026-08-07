package io.github.humphreymahlangu.votetrust.service;

import io.github.humphreymahlangu.votetrust.dto.ElectionResponse;
import io.github.humphreymahlangu.votetrust.entity.Election;
import io.github.humphreymahlangu.votetrust.exception.ResourceNotFoundException;
import io.github.humphreymahlangu.votetrust.repository.ElectionRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ElectionService {

    private final ElectionRepository electionRepository;

    public ElectionService(ElectionRepository electionRepository) {
        this.electionRepository = electionRepository;
    }

    @Transactional(readOnly = true)
    public List<ElectionResponse> listElections() {
        return electionRepository.findAllByOrderByRegistrationStartAtDesc()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public ElectionResponse getElection(UUID electionId) {
        Election election = electionRepository.findById(electionId)
                .orElseThrow(() -> new ResourceNotFoundException("Election not found"));
        return toResponse(election);
    }

    private ElectionResponse toResponse(Election election) {
        return new ElectionResponse(
                election.getId(),
                election.getName(),
                election.getType().name(),
                election.getStatus().name(),
                election.getRegistrationStartAt(),
                election.getRegistrationEndAt(),
                election.getVotingStartAt(),
                election.getVotingEndAt()
        );
    }
}
