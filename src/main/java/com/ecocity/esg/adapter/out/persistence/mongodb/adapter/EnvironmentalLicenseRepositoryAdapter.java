package com.ecocity.esg.adapter.out.persistence.mongodb.adapter;

import com.ecocity.esg.adapter.out.persistence.mongodb.mapper.EnvironmentalLicensePersistenceMapper;
import com.ecocity.esg.adapter.out.persistence.mongodb.repository.EnvironmentalLicenseMongoRepository;
import com.ecocity.esg.application.port.out.EnvironmentalLicenseRepositoryPort;
import com.ecocity.esg.application.port.out.IdempotencyReservationPort;
import com.ecocity.esg.domain.model.EnvironmentalLicense;
import com.ecocity.esg.domain.model.LicenseStatus;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.time.Instant;

@Component
public class EnvironmentalLicenseRepositoryAdapter implements EnvironmentalLicenseRepositoryPort {

    private final EnvironmentalLicenseMongoRepository mongoRepository;
    private final EnvironmentalLicensePersistenceMapper mapper;
    private final IdempotencyReservationPort reservations;

    public EnvironmentalLicenseRepositoryAdapter(EnvironmentalLicenseMongoRepository mongoRepository,
                                                  EnvironmentalLicensePersistenceMapper mapper, IdempotencyReservationPort reservations) {
        this.mongoRepository = mongoRepository;
        this.mapper = mapper;
        this.reservations = reservations;
    }

    @Override
    public EnvironmentalLicense save(EnvironmentalLicense environmentalLicense) {
        try {
            return mapper.toDomain(mongoRepository.save(mapper.toDocument(environmentalLicense)));
        } catch (org.springframework.dao.DuplicateKeyException ex) {
            if (environmentalLicense.getId() != null && environmentalLicense.getId().startsWith("idem-")) {
                return mongoRepository.findById(environmentalLicense.getId()).map(mapper::toDomain).orElseThrow(() -> ex);
            }
            throw ex;
        }
    }

    @Override
    public Optional<EnvironmentalLicense> findById(String id) {
        return mongoRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public List<EnvironmentalLicense> findAll(int page, int size) {
        return mongoRepository.findAll(PageRequest.of(page, size, Sort.by("id").ascending()))
                .map(mapper::toDomain)
                .getContent();
    }

    @Override
    public List<EnvironmentalLicense> findRenewalCandidatesBetween(Instant now, Instant deadline, int page, int size) {
        return mongoRepository.findRenewalCandidates(
                now, deadline, List.of(LicenseStatus.SUSPENDED, LicenseStatus.RENEWAL_IN_PROGRESS),
                PageRequest.of(page, size, Sort.by("id").ascending())).stream().map(mapper::toDomain).toList();
    }

    @Override
    public void delete(EnvironmentalLicense environmentalLicense) {
        reservations.markDeleted(environmentalLicense.getId());
        mongoRepository.delete(mapper.toDocument(environmentalLicense));
    }

}
