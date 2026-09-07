package com.ecocity.esg.adapter.in.web.mapper;

import com.ecocity.esg.adapter.in.web.dto.request.EnergyConsumptionRequest;
import com.ecocity.esg.adapter.in.web.dto.response.EnergyConsumptionResponse;
import com.ecocity.esg.domain.model.EnergyConsumption;
import org.springframework.stereotype.Component;

@Component
public class EnergyConsumptionWebMapper {

    public EnergyConsumption toDomain(EnergyConsumptionRequest request) {
        return EnergyConsumption.builder()
                .facilityId(request.getFacilityId())
                .facilityName(request.getFacilityName())
                .city(request.getCity())
                .sourceType(request.getSourceType())
                .consumptionKwh(request.getConsumptionKwh())
                .thresholdKwh(request.getThresholdKwh())
                .readingTimestamp(request.getReadingTimestamp())
                .sensorMetadata(request.getSensorMetadata())
                .build();
    }

    public EnergyConsumptionResponse toResponse(EnergyConsumption domain) {
        return EnergyConsumptionResponse.builder()
                .id(domain.getId())
                .facilityId(domain.getFacilityId())
                .facilityName(domain.getFacilityName())
                .city(domain.getCity())
                .sourceType(domain.getSourceType())
                .consumptionKwh(domain.getConsumptionKwh())
                .thresholdKwh(domain.getThresholdKwh())
                .alertTriggered(domain.isAlertTriggered())
                .readingTimestamp(domain.getReadingTimestamp())
                .sensorMetadata(domain.getSensorMetadata())
                .build();
    }
}
