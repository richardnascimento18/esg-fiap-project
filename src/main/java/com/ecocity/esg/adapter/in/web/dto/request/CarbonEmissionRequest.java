package com.ecocity.esg.adapter.in.web.dto.request;

import com.ecocity.esg.domain.model.EmissionType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
public class CarbonEmissionRequest {

    @NotBlank
    private String sourceFacility;

    @NotNull
    private EmissionType emissionType;

    @PositiveOrZero
    private double emissionTonnes;

    @PositiveOrZero
    private double compensationTonnes;

    @NotBlank
    private String reportingPeriod;

    @NotBlank
    private String auditedBy;
}
