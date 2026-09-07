package com.ecocity.esg.adapter.in.web.mapper;

import com.ecocity.esg.adapter.in.web.dto.request.CarbonEmissionRequest;
import com.ecocity.esg.adapter.in.web.dto.response.CarbonEmissionResponse;
import com.ecocity.esg.domain.model.CarbonEmission;
import org.springframework.stereotype.Component;

@Component
public class CarbonEmissionWebMapper {

    public CarbonEmission toDomain(CarbonEmissionRequest request) {
        return CarbonEmission.builder()
                .sourceFacility(request.getSourceFacility())
                .emissionType(request.getEmissionType())
                .emissionTonnes(request.getEmissionTonnes())
                .compensationTonnes(request.getCompensationTonnes())
                .reportingPeriod(request.getReportingPeriod())
                .auditedBy(request.getAuditedBy())
                .build();
    }

    public CarbonEmissionResponse toResponse(CarbonEmission domain) {
        return CarbonEmissionResponse.builder()
                .id(domain.getId())
                .sourceFacility(domain.getSourceFacility())
                .emissionType(domain.getEmissionType())
                .emissionTonnes(domain.getEmissionTonnes())
                .compensationTonnes(domain.getCompensationTonnes())
                .compensated(domain.isCompensated())
                .reportingPeriod(domain.getReportingPeriod())
                .auditedBy(domain.getAuditedBy())
                .build();
    }
}
