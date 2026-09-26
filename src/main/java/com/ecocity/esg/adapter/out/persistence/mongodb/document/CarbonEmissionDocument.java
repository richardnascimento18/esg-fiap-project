package com.ecocity.esg.adapter.out.persistence.mongodb.document;

import com.ecocity.esg.domain.model.EmissionType;
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
@Document(collection = "carbon_emission")
public class CarbonEmissionDocument {

    @Id
    private String id;
    @Version
    private Long version;
    private String sourceFacility;
    private EmissionType emissionType;
    private double emissionTonnes;
    private double compensationTonnes;
    private boolean compensated;
    private String reportingPeriod;
    private String auditedBy;
}
