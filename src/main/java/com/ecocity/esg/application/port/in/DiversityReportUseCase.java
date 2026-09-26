package com.ecocity.esg.application.port.in;

import com.ecocity.esg.domain.model.DiversityReport;

import java.util.List;

public interface DiversityReportUseCase {

    DiversityReport create(DiversityReport diversityReport);

    DiversityReport createWithId(DiversityReport diversityReport, String id);

    DiversityReport update(String id, DiversityReport diversityReport, long expectedVersion);

    void delete(String id, long expectedVersion);

    DiversityReport findById(String id);

    List<DiversityReport> findAll(int page, int size);
}
