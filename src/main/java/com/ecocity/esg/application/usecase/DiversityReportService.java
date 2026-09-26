package com.ecocity.esg.application.usecase;

import com.ecocity.esg.application.port.in.DiversityReportUseCase;
import com.ecocity.esg.application.port.out.DiversityReportRepositoryPort;
import com.ecocity.esg.domain.exception.ResourceNotFoundException;
import com.ecocity.esg.domain.model.DiversityReport;

import java.util.List;

public class DiversityReportService implements DiversityReportUseCase {

    private static final String RESOURCE_NAME = "DiversityReport";

    private final DiversityReportRepositoryPort repositoryPort;

    public DiversityReportService(DiversityReportRepositoryPort repositoryPort) {
        this.repositoryPort = repositoryPort;
    }

    @Override
    public DiversityReport create(DiversityReport diversityReport) {
        return repositoryPort.save(diversityReport.forCreation());
    }

    @Override
    public DiversityReport update(String id, DiversityReport diversityReport) {
        diversityReport.validate();
        DiversityReport existing = findById(id);
        return repositoryPort.save(existing.updateWith(diversityReport));
    }

    @Override
    public DiversityReport update(String id, DiversityReport diversityReport, long expectedVersion) {
        diversityReport.validate();
        DiversityReport existing = findById(id);
        if (existing.getVersion() == null || existing.getVersion() != expectedVersion) {
            throw new com.ecocity.esg.domain.exception.StaleResourceException();
        }
        return repositoryPort.save(existing.updateWith(diversityReport));
    }

    @Override
    public void delete(String id) {
        assertExists(id);
        repositoryPort.deleteById(id);
    }

    @Override
    public DiversityReport findById(String id) {
        return repositoryPort.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(RESOURCE_NAME, id));
    }

    @Override
    public List<DiversityReport> findAll(int page, int size) {
        return repositoryPort.findAll(page, size);
    }

    private void assertExists(String id) {
        if (!repositoryPort.existsById(id)) {
            throw new ResourceNotFoundException(RESOURCE_NAME, id);
        }
    }
}
