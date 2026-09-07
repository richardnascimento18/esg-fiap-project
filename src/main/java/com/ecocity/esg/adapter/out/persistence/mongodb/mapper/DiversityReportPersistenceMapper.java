package com.ecocity.esg.adapter.out.persistence.mongodb.mapper;

import com.ecocity.esg.adapter.out.persistence.mongodb.document.DiversityReportDocument;
import com.ecocity.esg.domain.model.DiversityReport;
import org.springframework.stereotype.Component;

@Component
public class DiversityReportPersistenceMapper {

    public DiversityReportDocument toDocument(DiversityReport domain) {
        return DiversityReportDocument.builder()
                .id(domain.getId())
                .department(domain.getDepartment())
                .totalEmployees(domain.getTotalEmployees())
                .womenPercentage(domain.getWomenPercentage())
                .blackAndMixedRacePercentage(domain.getBlackAndMixedRacePercentage())
                .personsWithDisabilitiesPercentage(domain.getPersonsWithDisabilitiesPercentage())
                .lgbtqiaPercentage(domain.getLgbtqiaPercentage())
                .reportingMonth(domain.getReportingMonth())
                .diversityTrainingCompleted(domain.isDiversityTrainingCompleted())
                .build();
    }

    public DiversityReport toDomain(DiversityReportDocument document) {
        return DiversityReport.builder()
                .id(document.getId())
                .department(document.getDepartment())
                .totalEmployees(document.getTotalEmployees())
                .womenPercentage(document.getWomenPercentage())
                .blackAndMixedRacePercentage(document.getBlackAndMixedRacePercentage())
                .personsWithDisabilitiesPercentage(document.getPersonsWithDisabilitiesPercentage())
                .lgbtqiaPercentage(document.getLgbtqiaPercentage())
                .reportingMonth(document.getReportingMonth())
                .diversityTrainingCompleted(document.isDiversityTrainingCompleted())
                .build();
    }
}
