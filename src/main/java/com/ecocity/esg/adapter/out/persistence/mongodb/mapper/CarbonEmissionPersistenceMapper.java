package com.ecocity.esg.adapter.out.persistence.mongodb.mapper;

import com.ecocity.esg.adapter.out.persistence.mongodb.document.CarbonEmissionDocument;
import com.ecocity.esg.domain.model.CarbonEmission;
import org.springframework.stereotype.Component;

@Component
public class CarbonEmissionPersistenceMapper {

    public CarbonEmissionDocument toDocument(CarbonEmission domain) {
        return CarbonEmissionDocument.builder()
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

    public CarbonEmission toDomain(CarbonEmissionDocument document) {
        return CarbonEmission.builder()
                .id(document.getId())
                .sourceFacility(document.getSourceFacility())
                .emissionType(document.getEmissionType())
                .emissionTonnes(document.getEmissionTonnes())
                .compensationTonnes(document.getCompensationTonnes())
                .compensated(document.isCompensated())
                .reportingPeriod(document.getReportingPeriod())
                .auditedBy(document.getAuditedBy())
                .build();
    }
}
