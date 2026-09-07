package com.ecocity.esg.adapter.in.web.dto.response;

import com.ecocity.esg.domain.model.EnergySourceType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.Map;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EnergyConsumptionResponse {

    private String id;
    private String facilityId;
    private String facilityName;
    private String city;
    private EnergySourceType sourceType;
    private double consumptionKwh;
    private double thresholdKwh;
    @Schema(description = "Verdadeiro somente quando consumptionKwh é maior que thresholdKwh.", accessMode = Schema.AccessMode.READ_ONLY)
    private boolean alertTriggered;
    private Instant readingTimestamp;
    private Map<String, Object> sensorMetadata;
}
