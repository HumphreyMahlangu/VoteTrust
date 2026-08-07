package io.github.humphreymahlangu.votetrust.service;

import io.github.humphreymahlangu.votetrust.dto.VotingDistrictResponse;
import io.github.humphreymahlangu.votetrust.entity.VotingDistrict;
import io.github.humphreymahlangu.votetrust.repository.VotingDistrictRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class VotingDistrictService {

    private final VotingDistrictRepository votingDistrictRepository;

    public VotingDistrictService(VotingDistrictRepository votingDistrictRepository) {
        this.votingDistrictRepository = votingDistrictRepository;
    }

    @Transactional(readOnly = true)
    public List<VotingDistrictResponse> listVotingDistricts() {
        return votingDistrictRepository.findAllByOrderByProvinceAscMunicipalityAscWardNumberAscNameAsc()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private VotingDistrictResponse toResponse(VotingDistrict votingDistrict) {
        return new VotingDistrictResponse(
                votingDistrict.getId(),
                votingDistrict.getCode(),
                votingDistrict.getName(),
                votingDistrict.getProvince(),
                votingDistrict.getMunicipality(),
                votingDistrict.getWardNumber()
        );
    }
}
