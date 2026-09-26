package com.ecocity.esg.adapter.out.persistence.mongodb.adapter;

import com.ecocity.esg.adapter.out.persistence.mongodb.mapper.WasteCollectionPersistenceMapper;
import com.ecocity.esg.adapter.out.persistence.mongodb.repository.WasteCollectionMongoRepository;
import com.ecocity.esg.application.port.out.WasteCollectionRepositoryPort;
import com.ecocity.esg.application.port.out.IdempotencyReservationPort;
import com.ecocity.esg.domain.model.WasteCollection;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class WasteCollectionRepositoryAdapter implements WasteCollectionRepositoryPort {

    private final WasteCollectionMongoRepository mongoRepository;
    private final WasteCollectionPersistenceMapper mapper;
    private final IdempotencyReservationPort reservations;

    public WasteCollectionRepositoryAdapter(WasteCollectionMongoRepository mongoRepository,
                                             WasteCollectionPersistenceMapper mapper, IdempotencyReservationPort reservations) {
        this.mongoRepository = mongoRepository;
        this.mapper = mapper;
        this.reservations = reservations;
    }

    @Override
    public WasteCollection save(WasteCollection wasteCollection) {
        try {
            return mapper.toDomain(mongoRepository.save(mapper.toDocument(wasteCollection)));
        } catch (org.springframework.dao.DuplicateKeyException ex) {
            if (wasteCollection.getId() != null && wasteCollection.getId().startsWith("idem-")) {
                return mongoRepository.findById(wasteCollection.getId()).map(mapper::toDomain).orElseThrow(() -> ex);
            }
            throw ex;
        }
    }

    @Override
    public Optional<WasteCollection> findById(String id) {
        return mongoRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public List<WasteCollection> findAll(int page, int size) {
        return mongoRepository.findAll(PageRequest.of(page, size, Sort.by("id").ascending()))
                .map(mapper::toDomain)
                .getContent();
    }

    @Override
    public void delete(WasteCollection wasteCollection) {
        reservations.markDeleted(wasteCollection.getId());
        mongoRepository.delete(mapper.toDocument(wasteCollection));
    }

}
