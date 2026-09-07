package com.ecocity.esg.adapter.in.web.dto.request;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DiversityReportRequest {

    @NotBlank
    private String department;

    @PositiveOrZero
    private int totalEmployees;

    @DecimalMin("0.0")
    @DecimalMax("100.0")
    private double womenPercentage;

    @DecimalMin("0.0")
    @DecimalMax("100.0")
    private double blackAndMixedRacePercentage;

    @DecimalMin("0.0")
    @DecimalMax("100.0")
    private double personsWithDisabilitiesPercentage;

    @DecimalMin("0.0")
    @DecimalMax("100.0")
    private double lgbtqiaPercentage;

    @NotBlank
    private String reportingMonth;

    private boolean diversityTrainingCompleted;
}
