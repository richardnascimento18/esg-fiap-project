package com.ecocity.esg.application.port.in;

import com.ecocity.esg.domain.model.CarbonEmission;

import java.util.List;

public interface CarbonEmissionUseCase {

    CarbonEmission create(CarbonEmission carbonEmission);

    CarbonEmission update(String id, CarbonEmission carbonEmission);

    void delete(String id);

    CarbonEmission findById(String id);

    List<CarbonEmission> findAll(int page, int size);
}
