package com.ecocity.esg.application.port.out;

import com.ecocity.esg.domain.model.DiversityReport;

import java.util.List;
import java.util.Optional;

public interface DiversityReportRepositoryPort {

    DiversityReport save(DiversityReport diversityReport);

    Optional<DiversityReport> findById(String id);

    List<DiversityReport> findAll(int page, int size);

    void deleteById(String id);

    void delete(DiversityReport diversityReport);

    boolean existsById(String id);
}
