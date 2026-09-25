package com.ecocity.esg.adapter.out.persistence.mongodb.adapter;

import com.ecocity.esg.adapter.out.persistence.mongodb.mapper.WasteCollectionPersistenceMapper;
import com.ecocity.esg.adapter.out.persistence.mongodb.repository.WasteCollectionMongoRepository;
import com.ecocity.esg.application.port.out.WasteCollectionRepositoryPort;
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

    public WasteCollectionRepositoryAdapter(WasteCollectionMongoRepository mongoRepository,
                                             WasteCollectionPersistenceMapper mapper) {
        this.mongoRepository = mongoRepository;
        this.mapper = mapper;
    }

    @Override
    public WasteCollection save(WasteCollection wasteCollection) {
        return mapper.toDomain(mongoRepository.save(mapper.toDocument(wasteCollection)));
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
    public void deleteById(String id) {
        mongoRepository.deleteById(id);
    }

    @Override
    public boolean existsById(String id) {
        return mongoRepository.existsById(id);
    }
}
