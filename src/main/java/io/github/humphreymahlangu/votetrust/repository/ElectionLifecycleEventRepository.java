package io.github.humphreymahlangu.votetrust.repository;

import io.github.humphreymahlangu.votetrust.entity.ElectionLifecycleEvent;
import io.github.humphreymahlangu.votetrust.entity.ElectionLifecycleOutcome;
import io.github.humphreymahlangu.votetrust.entity.ElectionStatus;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ElectionLifecycleEventRepository extends JpaRepository<ElectionLifecycleEvent, UUID> {

    List<ElectionLifecycleEvent> findByElectionIdOrderByEventSequenceAsc(UUID electionId);

    boolean existsByElectionIdAndPreviousStatusAndOutcome(
            UUID electionId,
            ElectionStatus previousStatus,
            ElectionLifecycleOutcome outcome
    );
}
