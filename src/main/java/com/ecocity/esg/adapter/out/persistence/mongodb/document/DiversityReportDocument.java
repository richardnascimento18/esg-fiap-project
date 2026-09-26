package com.ecocity.esg.adapter.out.persistence.mongodb.document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "diversity_report")
public class DiversityReportDocument {

    @Id
    private String id;
    @Version
    private Long version;
    private String department;
    private int totalEmployees;
    private double womenPercentage;
    private double blackAndMixedRacePercentage;
    private double personsWithDisabilitiesPercentage;
    private double lgbtqiaPercentage;
    private String reportingMonth;
    private boolean diversityTrainingCompleted;
}
