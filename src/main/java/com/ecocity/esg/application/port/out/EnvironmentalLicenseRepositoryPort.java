package com.ecocity.esg.application.port.out;

import com.ecocity.esg.domain.model.EnvironmentalLicense;

import java.util.List;
import java.util.Optional;

public interface EnvironmentalLicenseRepositoryPort {

    EnvironmentalLicense save(EnvironmentalLicense environmentalLicense);

    Optional<EnvironmentalLicense> findById(String id);

    List<EnvironmentalLicense> findAll(int page, int size);

    void deleteById(String id);

    boolean existsById(String id);
}
