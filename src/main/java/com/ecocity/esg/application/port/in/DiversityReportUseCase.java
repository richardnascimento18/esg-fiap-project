package com.ecocity.esg.application.port.in;

import com.ecocity.esg.domain.model.DiversityReport;

import java.util.List;

public interface DiversityReportUseCase {

    DiversityReport create(DiversityReport diversityReport);

    DiversityReport update(String id, DiversityReport diversityReport);

    void delete(String id);

    DiversityReport findById(String id);

    List<DiversityReport> findAll(int page, int size);
}
