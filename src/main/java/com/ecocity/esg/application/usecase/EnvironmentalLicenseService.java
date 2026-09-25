package com.ecocity.esg.application.usecase;

import com.ecocity.esg.application.port.in.EnvironmentalLicenseUseCase;
import com.ecocity.esg.application.port.out.EnvironmentalLicenseRepositoryPort;
import com.ecocity.esg.domain.exception.ResourceNotFoundException;
import com.ecocity.esg.domain.model.EnvironmentalLicense;

import java.time.Clock;
import java.util.List;

public class EnvironmentalLicenseService implements EnvironmentalLicenseUseCase {

    private static final String RESOURCE_NAME = "EnvironmentalLicense";

    private final EnvironmentalLicenseRepositoryPort repositoryPort;
    private final Clock clock;

    public EnvironmentalLicenseService(EnvironmentalLicenseRepositoryPort repositoryPort, Clock clock) {
        this.repositoryPort = repositoryPort;
        this.clock = clock;
    }

    @Override
    public EnvironmentalLicense create(EnvironmentalLicense environmentalLicense) {
        return repositoryPort.save(environmentalLicense.forCreation(clock.instant()));
    }

    @Override
    public EnvironmentalLicense update(String id, EnvironmentalLicense environmentalLicense) {
        environmentalLicense.validate();
        EnvironmentalLicense existing = repositoryPort.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(RESOURCE_NAME, id));
        return repositoryPort.save(existing.updateWith(environmentalLicense, clock.instant()));
    }

    @Override
    public void delete(String id) {
        assertExists(id);
        repositoryPort.deleteById(id);
    }

    @Override
    public EnvironmentalLicense findById(String id) {
        return repositoryPort.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(RESOURCE_NAME, id))
                .withEffectiveStatusAt(clock.instant());
    }

    @Override
    public List<EnvironmentalLicense> findAll(int page, int size) {
        return repositoryPort.findAll(page, size).stream()
                .map(license -> license.withEffectiveStatusAt(clock.instant())).toList();
    }

    private void assertExists(String id) {
        if (!repositoryPort.existsById(id)) {
            throw new ResourceNotFoundException(RESOURCE_NAME, id);
        }
    }
}
