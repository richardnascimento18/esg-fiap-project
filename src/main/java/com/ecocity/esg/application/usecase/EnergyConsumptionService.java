package com.ecocity.esg.application.usecase;

import com.ecocity.esg.application.port.in.EnergyConsumptionUseCase;
import com.ecocity.esg.application.port.out.EnergyConsumptionRepositoryPort;
import com.ecocity.esg.domain.exception.ResourceNotFoundException;
import com.ecocity.esg.domain.model.EnergyConsumption;

import java.util.List;

public class EnergyConsumptionService implements EnergyConsumptionUseCase {

    private static final String RESOURCE_NAME = "EnergyConsumption";

    private final EnergyConsumptionRepositoryPort repositoryPort;

    public EnergyConsumptionService(EnergyConsumptionRepositoryPort repositoryPort) {
        this.repositoryPort = repositoryPort;
    }

    @Override
    public EnergyConsumption create(EnergyConsumption energyConsumption) {
        return repositoryPort.save(energyConsumption.forCreation());
    }

    @Override
    public EnergyConsumption update(String id, EnergyConsumption energyConsumption) {
        EnergyConsumption existing = findById(id);
        return repositoryPort.save(existing.updateWith(energyConsumption));
    }

    @Override
    public EnergyConsumption update(String id, EnergyConsumption energyConsumption, long expectedVersion) {
        energyConsumption.validate();
        EnergyConsumption existing = findById(id);
        if (existing.getVersion() == null || existing.getVersion() != expectedVersion) {
            throw new com.ecocity.esg.domain.exception.StaleResourceException();
        }
        return repositoryPort.save(existing.updateWith(energyConsumption));
    }

    @Override
    public void delete(String id) {
        assertExists(id);
        repositoryPort.deleteById(id);
    }

    @Override
    public EnergyConsumption findById(String id) {
        return repositoryPort.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(RESOURCE_NAME, id));
    }

    @Override
    public List<EnergyConsumption> findAll(int page, int size) {
        return repositoryPort.findAll(page, size);
    }

    private void assertExists(String id) {
        if (!repositoryPort.existsById(id)) {
            throw new ResourceNotFoundException(RESOURCE_NAME, id);
        }
    }
}
