package com.ecocity.esg.adapter.in.web.dto.request;

import com.ecocity.esg.domain.model.EnergySourceType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.Map;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EnergyConsumptionRequest {

    @NotBlank
    private String facilityId;

    @NotBlank
    private String facilityName;

    @NotBlank
    private String city;

    @NotNull
    private EnergySourceType sourceType;

    @PositiveOrZero
    private double consumptionKwh;

    @PositiveOrZero
    private double thresholdKwh;

    @NotNull
    private Instant readingTimestamp;

    private Map<String, Object> sensorMetadata;
}
