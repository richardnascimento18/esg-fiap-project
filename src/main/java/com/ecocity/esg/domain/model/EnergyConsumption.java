package com.ecocity.esg.domain.model;

import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Environmental pillar: tracks the energy consumption of a facility or IoT sensor
 * in the smart city and flags automatic alerts when a consumption threshold is crossed.
 */
@Getter
public final class EnergyConsumption {

    private final String id;
    private final Long version;
    private final String facilityId;
    private final String facilityName;
    private final String city;
    private final EnergySourceType sourceType;
    private final double consumptionKwh;
    private final double thresholdKwh;
    private final boolean alertTriggered;
    private final Instant readingTimestamp;
    private final Map<String, Object> sensorMetadata;

    // The builder also restores stored state; write operations below apply business rules.
    @Builder(toBuilder = true)
    private EnergyConsumption(String id,
            Long version,
            String facilityId,
            String facilityName,
            String city,
            EnergySourceType sourceType,
            double consumptionKwh,
            double thresholdKwh,
            boolean alertTriggered,
            Instant readingTimestamp,
            Map<String, Object> sensorMetadata) {
        this.id = id;
        this.version = version;
        this.facilityId = facilityId;
        this.facilityName = facilityName;
        this.city = city;
        this.sourceType = sourceType;
        this.consumptionKwh = consumptionKwh;
        this.thresholdKwh = thresholdKwh;
        this.alertTriggered = alertTriggered;
        this.readingTimestamp = readingTimestamp;
        this.sensorMetadata = sensorMetadata == null
                ? null : Collections.unmodifiableMap(new LinkedHashMap<>(sensorMetadata));
    }

    public EnergyConsumption forCreation() {
        validate();
        return toBuilder().id(null).version(null).alertTriggered(consumptionKwh > thresholdKwh).build();
    }

    public EnergyConsumption updateWith(EnergyConsumption replacement) {
        replacement.validate();
        return replacement.toBuilder().id(id).version(version).alertTriggered(replacement.consumptionKwh > replacement.thresholdKwh).build();
    }

    public void validate() {
        DomainRules.requiredText(facilityId, "facilityId");
        DomainRules.requiredText(facilityName, "facilityName");
        DomainRules.requiredText(city, "city");
        DomainRules.required(sourceType, "sourceType");
        DomainRules.required(readingTimestamp, "readingTimestamp");
        DomainRules.nonNegative(consumptionKwh, "consumptionKwh");
        DomainRules.nonNegative(thresholdKwh, "thresholdKwh");
    }
}
