package io.github.humphreymahlangu.votetrust.repository;

import io.github.humphreymahlangu.votetrust.entity.VotingDistrict;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VotingDistrictRepository extends JpaRepository<VotingDistrict, UUID> {

    List<VotingDistrict> findAllByOrderByProvinceAscMunicipalityAscWardNumberAscNameAsc();

    boolean existsByCodeIgnoreCase(String code);
}
