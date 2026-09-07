package com.ecocity.esg.adapter.in.web.mapper;

import com.ecocity.esg.adapter.in.web.dto.request.DiversityReportRequest;
import com.ecocity.esg.adapter.in.web.dto.response.DiversityReportResponse;
import com.ecocity.esg.domain.model.DiversityReport;
import org.springframework.stereotype.Component;

@Component
public class DiversityReportWebMapper {

    public DiversityReport toDomain(DiversityReportRequest request) {
        return DiversityReport.builder()
                .department(request.getDepartment())
                .totalEmployees(request.getTotalEmployees())
                .womenPercentage(request.getWomenPercentage())
                .blackAndMixedRacePercentage(request.getBlackAndMixedRacePercentage())
                .personsWithDisabilitiesPercentage(request.getPersonsWithDisabilitiesPercentage())
                .lgbtqiaPercentage(request.getLgbtqiaPercentage())
                .reportingMonth(request.getReportingMonth())
                .diversityTrainingCompleted(request.isDiversityTrainingCompleted())
                .build();
    }

    public DiversityReportResponse toResponse(DiversityReport domain) {
        return DiversityReportResponse.builder()
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
}
