package com.ecocity.esg.application.port.out;

import com.ecocity.esg.domain.model.CarbonEmission;

import java.util.List;
import java.util.Optional;

public interface CarbonEmissionRepositoryPort {

    CarbonEmission save(CarbonEmission carbonEmission);

    Optional<CarbonEmission> findById(String id);

    List<CarbonEmission> findAll(int page, int size);

    void deleteById(String id);

    boolean existsById(String id);
}
