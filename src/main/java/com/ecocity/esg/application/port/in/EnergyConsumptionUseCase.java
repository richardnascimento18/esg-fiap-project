package com.ecocity.esg.application.port.in;

import com.ecocity.esg.domain.model.EnergyConsumption;

import java.util.List;

/**
 * Primary (driving) port that exposes the business capabilities available for the
 * EnergyConsumption aggregate. Adapters such as the REST controller depend on this
 * interface only, never on the concrete use case implementation.
 */
public interface EnergyConsumptionUseCase {

    EnergyConsumption create(EnergyConsumption energyConsumption);

    EnergyConsumption createWithId(EnergyConsumption energyConsumption, String id);

    EnergyConsumption update(String id, EnergyConsumption energyConsumption);

    EnergyConsumption update(String id, EnergyConsumption energyConsumption, long expectedVersion);

    void delete(String id);

    void delete(String id, long expectedVersion);

    EnergyConsumption findById(String id);

    List<EnergyConsumption> findAll(int page, int size);
}
