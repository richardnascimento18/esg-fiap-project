package com.ecocity.esg.domain.model;

import lombok.Builder;
import lombok.Getter;

/**
 * Governance pillar: monitors carbon emissions per facility and their
 * corresponding environmental compensation, supporting automated compliance reporting.
 */
@Getter
public final class CarbonEmission {

    private final String id;
    private final Long version;
    private final String sourceFacility;
    private final EmissionType emissionType;
    private final double emissionTonnes;
    private final double compensationTonnes;
    private final boolean compensated;
    private final String reportingPeriod;
    private final String auditedBy;

    // The builder also restores stored state; write operations below apply business rules.
    @Builder(toBuilder = true)
    private CarbonEmission(String id,
            Long version,
            String sourceFacility,
            EmissionType emissionType,
            double emissionTonnes,
            double compensationTonnes,
            boolean compensated,
            String reportingPeriod,
            String auditedBy) {
        this.id = id;
        this.version = version;
        this.sourceFacility = sourceFacility;
        this.emissionType = emissionType;
        this.emissionTonnes = emissionTonnes;
        this.compensationTonnes = compensationTonnes;
        this.compensated = compensated;
        this.reportingPeriod = reportingPeriod;
        this.auditedBy = auditedBy;
    }

    public CarbonEmission forCreation() {
        validate();
        return toBuilder().id(null).version(null).compensated(compensationTonnes >= emissionTonnes).build();
    }

    public CarbonEmission updateWith(CarbonEmission replacement) {
        replacement.validate();
        return replacement.toBuilder().id(id).version(version).compensated(replacement.compensationTonnes >= replacement.emissionTonnes).build();
    }

    public void validate() {
        DomainRules.nonNegative(emissionTonnes, "emissionTonnes");
        DomainRules.nonNegative(compensationTonnes, "compensationTonnes");
        DomainRules.reportingQuarter(reportingPeriod);
    }
}
