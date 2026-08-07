package io.github.humphreymahlangu.votetrust.service;

import io.github.humphreymahlangu.votetrust.dto.ContestOptionResponse;
import io.github.humphreymahlangu.votetrust.dto.ContestResponse;
import io.github.humphreymahlangu.votetrust.entity.Contest;
import io.github.humphreymahlangu.votetrust.entity.ContestOption;
import io.github.humphreymahlangu.votetrust.exception.ResourceNotFoundException;
import io.github.humphreymahlangu.votetrust.repository.ContestOptionRepository;
import io.github.humphreymahlangu.votetrust.repository.ContestRepository;
import io.github.humphreymahlangu.votetrust.repository.ElectionRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ContestService {

    private final ElectionRepository electionRepository;
    private final ContestRepository contestRepository;
    private final ContestOptionRepository contestOptionRepository;

    public ContestService(
            ElectionRepository electionRepository,
            ContestRepository contestRepository,
            ContestOptionRepository contestOptionRepository
    ) {
        this.electionRepository = electionRepository;
        this.contestRepository = contestRepository;
        this.contestOptionRepository = contestOptionRepository;
    }

    @Transactional(readOnly = true)
    public List<ContestResponse> listContests(UUID electionId) {
        if (!electionRepository.existsById(electionId)) {
            throw new ResourceNotFoundException("Election not found");
        }

        return contestRepository.findByElectionIdOrderByDisplayOrderAscNameAsc(electionId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private ContestResponse toResponse(Contest contest) {
        return new ContestResponse(
                contest.getId(),
                contest.getElection().getId(),
                contest.getName(),
                contest.getType().name(),
                contest.getStatus().name(),
                contest.getDisplayOrder(),
                contestOptionRepository.findByContestIdOrderByDisplayOrderAscNameAsc(contest.getId())
                        .stream()
                        .map(this::toOptionResponse)
                        .toList()
        );
    }

    private ContestOptionResponse toOptionResponse(ContestOption contestOption) {
        return new ContestOptionResponse(
                contestOption.getId(),
                contestOption.getName(),
                contestOption.getOptionType().name(),
                contestOption.getDisplayOrder()
        );
    }
}
