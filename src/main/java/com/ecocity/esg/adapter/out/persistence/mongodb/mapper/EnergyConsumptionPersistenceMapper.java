package com.ecocity.esg.adapter.out.persistence.mongodb.mapper;

import com.ecocity.esg.adapter.out.persistence.mongodb.document.EnergyConsumptionDocument;
import com.ecocity.esg.domain.model.EnergyConsumption;
import org.springframework.stereotype.Component;

@Component
public class EnergyConsumptionPersistenceMapper {

    public EnergyConsumptionDocument toDocument(EnergyConsumption domain) {
        return EnergyConsumptionDocument.builder()
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

    public EnergyConsumption toDomain(EnergyConsumptionDocument document) {
        return EnergyConsumption.builder()
                .id(document.getId())
                .facilityId(document.getFacilityId())
                .facilityName(document.getFacilityName())
                .city(document.getCity())
                .sourceType(document.getSourceType())
                .consumptionKwh(document.getConsumptionKwh())
                .thresholdKwh(document.getThresholdKwh())
                .alertTriggered(document.isAlertTriggered())
                .readingTimestamp(document.getReadingTimestamp())
                .sensorMetadata(document.getSensorMetadata())
                .build();
    }
}
