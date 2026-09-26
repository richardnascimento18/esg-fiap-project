package com.ecocity.esg.adapter.out.persistence.mongodb.adapter;

import com.ecocity.esg.adapter.out.persistence.mongodb.mapper.CarbonEmissionPersistenceMapper;
import com.ecocity.esg.adapter.out.persistence.mongodb.repository.CarbonEmissionMongoRepository;
import com.ecocity.esg.application.port.out.CarbonEmissionRepositoryPort;
import com.ecocity.esg.application.port.out.IdempotencyReservationPort;
import com.ecocity.esg.domain.model.CarbonEmission;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class CarbonEmissionRepositoryAdapter implements CarbonEmissionRepositoryPort {

    private final CarbonEmissionMongoRepository mongoRepository;
    private final CarbonEmissionPersistenceMapper mapper;
    private final IdempotencyReservationPort reservations;

    public CarbonEmissionRepositoryAdapter(CarbonEmissionMongoRepository mongoRepository,
                                            CarbonEmissionPersistenceMapper mapper, IdempotencyReservationPort reservations) {
        this.mongoRepository = mongoRepository;
        this.mapper = mapper;
        this.reservations = reservations;
    }

    @Override
    public CarbonEmission save(CarbonEmission carbonEmission) {
        try {
            return mapper.toDomain(mongoRepository.save(mapper.toDocument(carbonEmission)));
        } catch (org.springframework.dao.DuplicateKeyException ex) {
            if (carbonEmission.getId() != null && carbonEmission.getId().startsWith("idem-")) {
                return mongoRepository.findById(carbonEmission.getId()).map(mapper::toDomain).orElseThrow(() -> ex);
            }
            throw ex;
        }
    }

    @Override
    public Optional<CarbonEmission> findById(String id) {
        return mongoRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public List<CarbonEmission> findAll(int page, int size) {
        return mongoRepository.findAll(PageRequest.of(page, size, Sort.by("id").ascending()))
                .map(mapper::toDomain)
                .getContent();
    }

    @Override
    public void delete(CarbonEmission carbonEmission) {
        reservations.markDeleted(carbonEmission.getId());
        mongoRepository.delete(mapper.toDocument(carbonEmission));
    }

}
