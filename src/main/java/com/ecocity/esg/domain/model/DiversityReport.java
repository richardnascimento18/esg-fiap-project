package com.ecocity.esg.domain.model;

import com.ecocity.esg.domain.exception.DomainValidationException;
import lombok.Builder;
import lombok.Getter;

/**
 * Social pillar: consolidates diversity and inclusion indicators per department,
 * supporting recruitment and mandatory training policies.
 */
@Getter
public final class DiversityReport {

    private final String id;
    private final String department;
    private final int totalEmployees;
    private final double womenPercentage;
    private final double blackAndMixedRacePercentage;
    private final double personsWithDisabilitiesPercentage;
    private final double lgbtqiaPercentage;
    private final String reportingMonth;
    private final boolean diversityTrainingCompleted;

    // The builder also restores stored state; write operations below apply business rules.
    @Builder(toBuilder = true)
    private DiversityReport(String id,
            String department,
            int totalEmployees,
            double womenPercentage,
            double blackAndMixedRacePercentage,
            double personsWithDisabilitiesPercentage,
            double lgbtqiaPercentage,
            String reportingMonth,
            boolean diversityTrainingCompleted) {
        this.id = id;
        this.department = department;
        this.totalEmployees = totalEmployees;
        this.womenPercentage = womenPercentage;
        this.blackAndMixedRacePercentage = blackAndMixedRacePercentage;
        this.personsWithDisabilitiesPercentage = personsWithDisabilitiesPercentage;
        this.lgbtqiaPercentage = lgbtqiaPercentage;
        this.reportingMonth = reportingMonth;
        this.diversityTrainingCompleted = diversityTrainingCompleted;
    }

    public DiversityReport forCreation() {
        validate();
        return toBuilder().id(null).build();
    }

    public DiversityReport updateWith(DiversityReport replacement) {
        replacement.validate();
        return replacement.toBuilder().id(id).build();
    }

    public void validate() {
        if (totalEmployees < 0) {
            throw new DomainValidationException("totalEmployees deve ser nao negativo");
        }
        DomainRules.percentage(womenPercentage, "womenPercentage");
        DomainRules.percentage(blackAndMixedRacePercentage, "blackAndMixedRacePercentage");
        DomainRules.percentage(personsWithDisabilitiesPercentage, "personsWithDisabilitiesPercentage");
        DomainRules.percentage(lgbtqiaPercentage, "lgbtqiaPercentage");
        DomainRules.reportingMonth(reportingMonth);
    }
}
