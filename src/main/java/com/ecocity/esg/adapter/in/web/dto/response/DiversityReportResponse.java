package com.ecocity.esg.adapter.in.web.dto.response;

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
public class DiversityReportResponse {

    private String id;
    private String department;
    private int totalEmployees;
    private double womenPercentage;
    private double blackAndMixedRacePercentage;
    private double personsWithDisabilitiesPercentage;
    private double lgbtqiaPercentage;
    private String reportingMonth;
    private boolean diversityTrainingCompleted;
}
