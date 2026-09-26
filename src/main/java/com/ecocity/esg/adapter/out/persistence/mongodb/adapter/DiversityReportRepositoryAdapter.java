package com.ecocity.esg.adapter.out.persistence.mongodb.adapter;

import com.ecocity.esg.adapter.out.persistence.mongodb.mapper.DiversityReportPersistenceMapper;
import com.ecocity.esg.adapter.out.persistence.mongodb.repository.DiversityReportMongoRepository;
import com.ecocity.esg.application.port.out.DiversityReportRepositoryPort;
import com.ecocity.esg.application.port.out.IdempotencyReservationPort;
import com.ecocity.esg.domain.model.DiversityReport;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class DiversityReportRepositoryAdapter implements DiversityReportRepositoryPort {

    private final DiversityReportMongoRepository mongoRepository;
    private final DiversityReportPersistenceMapper mapper;
    private final IdempotencyReservationPort reservations;

    public DiversityReportRepositoryAdapter(DiversityReportMongoRepository mongoRepository,
                                             DiversityReportPersistenceMapper mapper, IdempotencyReservationPort reservations) {
        this.mongoRepository = mongoRepository;
        this.mapper = mapper;
        this.reservations = reservations;
    }

    @Override
    public DiversityReport save(DiversityReport diversityReport) {
        try {
            return mapper.toDomain(mongoRepository.save(mapper.toDocument(diversityReport)));
        } catch (org.springframework.dao.DuplicateKeyException ex) {
            if (diversityReport.getId() != null && diversityReport.getId().startsWith("idem-")) {
                return mongoRepository.findById(diversityReport.getId()).map(mapper::toDomain).orElseThrow(() -> ex);
            }
            throw ex;
        }
    }

    @Override
    public Optional<DiversityReport> findById(String id) {
        return mongoRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public List<DiversityReport> findAll(int page, int size) {
        return mongoRepository.findAll(PageRequest.of(page, size, Sort.by("id").ascending()))
                .map(mapper::toDomain)
                .getContent();
    }

    @Override
    public void deleteById(String id) {
        mongoRepository.deleteById(id);
    }

    @Override
    public void delete(DiversityReport diversityReport) {
        reservations.markDeleted(diversityReport.getId());
        mongoRepository.delete(mapper.toDocument(diversityReport));
    }

    @Override
    public boolean existsById(String id) {
        return mongoRepository.existsById(id);
    }
}
