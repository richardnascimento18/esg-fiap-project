package com.ecocity.esg.application.port.out;

import com.ecocity.esg.domain.model.EnergyConsumption;

import java.util.List;
import java.util.Optional;

/**
 * Secondary (driven) port. The application core depends on this abstraction; the
 * MongoDB adapter is the concrete implementation plugged in at runtime.
 */
public interface EnergyConsumptionRepositoryPort {

    EnergyConsumption save(EnergyConsumption energyConsumption);

    Optional<EnergyConsumption> findById(String id);

    List<EnergyConsumption> findAll(int page, int size);

    void deleteById(String id);

    boolean existsById(String id);
}
