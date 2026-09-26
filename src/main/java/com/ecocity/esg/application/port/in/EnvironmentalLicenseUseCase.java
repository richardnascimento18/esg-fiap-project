package com.ecocity.esg.application.port.in;

import com.ecocity.esg.domain.model.EnvironmentalLicense;

import java.util.List;

public interface EnvironmentalLicenseUseCase {

    EnvironmentalLicense create(EnvironmentalLicense environmentalLicense);

    EnvironmentalLicense update(String id, EnvironmentalLicense environmentalLicense);

    EnvironmentalLicense update(String id, EnvironmentalLicense environmentalLicense, long expectedVersion);

    void delete(String id);

    EnvironmentalLicense findById(String id);

    List<EnvironmentalLicense> findAll(int page, int size);
}
