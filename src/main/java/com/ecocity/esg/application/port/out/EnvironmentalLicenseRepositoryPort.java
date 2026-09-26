package com.ecocity.esg.application.port.out;

import com.ecocity.esg.domain.model.EnvironmentalLicense;

import java.util.List;
import java.util.Optional;
import java.time.Instant;

public interface EnvironmentalLicenseRepositoryPort {

    EnvironmentalLicense save(EnvironmentalLicense environmentalLicense);

    Optional<EnvironmentalLicense> findById(String id);

    List<EnvironmentalLicense> findAll(int page, int size);

    List<EnvironmentalLicense> findRenewalCandidatesBetween(Instant now, Instant deadline, int page, int size);

    void deleteById(String id);

    void delete(EnvironmentalLicense environmentalLicense);

    boolean existsById(String id);
}
