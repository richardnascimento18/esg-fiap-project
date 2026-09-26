package com.ecocity.esg.application.usecase;

import com.ecocity.esg.application.port.in.CarbonEmissionUseCase;
import com.ecocity.esg.application.port.out.CarbonEmissionRepositoryPort;
import com.ecocity.esg.domain.exception.ResourceNotFoundException;
import com.ecocity.esg.domain.model.CarbonEmission;

import java.util.List;

public class CarbonEmissionService implements CarbonEmissionUseCase {

    private static final String RESOURCE_NAME = "CarbonEmission";

    private final CarbonEmissionRepositoryPort repositoryPort;

    public CarbonEmissionService(CarbonEmissionRepositoryPort repositoryPort) {
        this.repositoryPort = repositoryPort;
    }

    @Override
    public CarbonEmission create(CarbonEmission carbonEmission) {
        return repositoryPort.save(carbonEmission.forCreation());
    }

    @Override
    public CarbonEmission createWithId(CarbonEmission carbonEmission, String id) {
        CarbonEmission created = carbonEmission.forCreation();
        return repositoryPort.save(created.toBuilder().id(id).build());
    }

    @Override
    public CarbonEmission update(String id, CarbonEmission carbonEmission) {
        CarbonEmission existing = findById(id);
        return repositoryPort.save(existing.updateWith(carbonEmission));
    }

    @Override
    public CarbonEmission update(String id, CarbonEmission carbonEmission, long expectedVersion) {
        carbonEmission.validate();
        CarbonEmission existing = findById(id);
        if (existing.getVersion() == null || existing.getVersion() != expectedVersion) {
            throw new com.ecocity.esg.domain.exception.StaleResourceException();
        }
        return repositoryPort.save(existing.updateWith(carbonEmission));
    }

    @Override
    public void delete(String id) {
        assertExists(id);
        repositoryPort.deleteById(id);
    }

    @Override
    public void delete(String id, long expectedVersion) {
        CarbonEmission existing = findById(id);
        if (existing.getVersion() == null || existing.getVersion() != expectedVersion) {
            throw new com.ecocity.esg.domain.exception.StaleResourceException();
        }
        repositoryPort.delete(existing);
    }

    @Override
    public CarbonEmission findById(String id) {
        return repositoryPort.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(RESOURCE_NAME, id));
    }

    @Override
    public List<CarbonEmission> findAll(int page, int size) {
        return repositoryPort.findAll(page, size);
    }

    private void assertExists(String id) {
        if (!repositoryPort.existsById(id)) {
            throw new ResourceNotFoundException(RESOURCE_NAME, id);
        }
    }
}
