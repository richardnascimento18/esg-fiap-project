package com.ecocity.esg.adapter.in.web.dto.response;

import com.ecocity.esg.domain.model.EmissionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CarbonEmissionResponse {

    private String id;
    private String sourceFacility;
    private EmissionType emissionType;
    private double emissionTonnes;
    private double compensationTonnes;
    @Schema(description = "Verdadeiro quando compensationTonnes é maior ou igual a emissionTonnes.", accessMode = Schema.AccessMode.READ_ONLY)
    private boolean compensated;
    private String reportingPeriod;
    private String auditedBy;
}
