package com.ecocity.esg.adapter.out.persistence.mongodb.adapter;

import com.ecocity.esg.adapter.out.persistence.mongodb.mapper.EnvironmentalLicensePersistenceMapper;
import com.ecocity.esg.adapter.out.persistence.mongodb.repository.EnvironmentalLicenseMongoRepository;
import com.ecocity.esg.application.port.out.EnvironmentalLicenseRepositoryPort;
import com.ecocity.esg.domain.model.EnvironmentalLicense;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class EnvironmentalLicenseRepositoryAdapter implements EnvironmentalLicenseRepositoryPort {

    private final EnvironmentalLicenseMongoRepository mongoRepository;
    private final EnvironmentalLicensePersistenceMapper mapper;

    public EnvironmentalLicenseRepositoryAdapter(EnvironmentalLicenseMongoRepository mongoRepository,
                                                  EnvironmentalLicensePersistenceMapper mapper) {
        this.mongoRepository = mongoRepository;
        this.mapper = mapper;
    }

    @Override
    public EnvironmentalLicense save(EnvironmentalLicense environmentalLicense) {
        return mapper.toDomain(mongoRepository.save(mapper.toDocument(environmentalLicense)));
    }

    @Override
    public Optional<EnvironmentalLicense> findById(String id) {
        return mongoRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public List<EnvironmentalLicense> findAll(int page, int size) {
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
