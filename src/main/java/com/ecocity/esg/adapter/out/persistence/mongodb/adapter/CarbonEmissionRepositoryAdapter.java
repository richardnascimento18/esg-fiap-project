package com.ecocity.esg.adapter.out.persistence.mongodb.adapter;

import com.ecocity.esg.adapter.out.persistence.mongodb.mapper.CarbonEmissionPersistenceMapper;
import com.ecocity.esg.adapter.out.persistence.mongodb.repository.CarbonEmissionMongoRepository;
import com.ecocity.esg.application.port.out.CarbonEmissionRepositoryPort;
import com.ecocity.esg.domain.model.CarbonEmission;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class CarbonEmissionRepositoryAdapter implements CarbonEmissionRepositoryPort {

    private final CarbonEmissionMongoRepository mongoRepository;
    private final CarbonEmissionPersistenceMapper mapper;

    public CarbonEmissionRepositoryAdapter(CarbonEmissionMongoRepository mongoRepository,
                                            CarbonEmissionPersistenceMapper mapper) {
        this.mongoRepository = mongoRepository;
        this.mapper = mapper;
    }

    @Override
    public CarbonEmission save(CarbonEmission carbonEmission) {
        return mapper.toDomain(mongoRepository.save(mapper.toDocument(carbonEmission)));
    }

    @Override
    public Optional<CarbonEmission> findById(String id) {
        return mongoRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public List<CarbonEmission> findAll(int page, int size) {
        return mongoRepository.findAll(PageRequest.of(page, size))
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
