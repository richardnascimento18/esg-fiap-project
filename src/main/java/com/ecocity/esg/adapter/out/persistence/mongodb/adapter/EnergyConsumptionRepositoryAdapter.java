package com.ecocity.esg.adapter.out.persistence.mongodb.adapter;

import com.ecocity.esg.adapter.out.persistence.mongodb.mapper.EnergyConsumptionPersistenceMapper;
import com.ecocity.esg.adapter.out.persistence.mongodb.repository.EnergyConsumptionMongoRepository;
import com.ecocity.esg.application.port.out.EnergyConsumptionRepositoryPort;
import com.ecocity.esg.domain.model.EnergyConsumption;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class EnergyConsumptionRepositoryAdapter implements EnergyConsumptionRepositoryPort {

    private final EnergyConsumptionMongoRepository mongoRepository;
    private final EnergyConsumptionPersistenceMapper mapper;

    public EnergyConsumptionRepositoryAdapter(EnergyConsumptionMongoRepository mongoRepository,
                                               EnergyConsumptionPersistenceMapper mapper) {
        this.mongoRepository = mongoRepository;
        this.mapper = mapper;
    }

    @Override
    public EnergyConsumption save(EnergyConsumption energyConsumption) {
        return mapper.toDomain(mongoRepository.save(mapper.toDocument(energyConsumption)));
    }

    @Override
    public Optional<EnergyConsumption> findById(String id) {
        return mongoRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public List<EnergyConsumption> findAll(int page, int size) {
        return mongoRepository.findAll(PageRequest.of(page, size, Sort.by("id").ascending()))
                .map(mapper::toDomain)
                .getContent();
    }

    @Override
    public void deleteById(String id) {
        mongoRepository.deleteById(id);
    }

    @Override
    public boolean existsById(String id) {
        return mongoRepository.existsById(id);
    }
}
