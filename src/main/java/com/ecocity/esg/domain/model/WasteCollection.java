package com.ecocity.esg.domain.model;

import com.ecocity.esg.domain.exception.DomainValidationException;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

/**
 * Environmental pillar: tracks selective waste collection and recycling across
 * the city districts.
 */
@Getter
public final class WasteCollection {

    private final String id;
    private final String district;
    private final WasteType wasteType;
    private final double weightKg;
    private final double recyclingRatePercentage;
    private final Instant collectionDate;
    private final String collectorTeam;
    private final boolean properlyDisposed;

    // The builder also restores stored state; write operations below apply business rules.
    @Builder(toBuilder = true)
    private WasteCollection(String id,
            String district,
            WasteType wasteType,
            double weightKg,
            double recyclingRatePercentage,
            Instant collectionDate,
            String collectorTeam,
            boolean properlyDisposed) {
        this.id = id;
        this.district = district;
        this.wasteType = wasteType;
        this.weightKg = weightKg;
        this.recyclingRatePercentage = recyclingRatePercentage;
        this.collectionDate = collectionDate;
        this.collectorTeam = collectorTeam;
        this.properlyDisposed = properlyDisposed;
    }

    public WasteCollection forCreation() {
        validate();
        return toBuilder().id(null).build();
    }

    public WasteCollection updateWith(WasteCollection replacement) {
        replacement.validate();
        return replacement.toBuilder().id(id).build();
    }

    public void validate() {
        if (recyclingRatePercentage < 0 || recyclingRatePercentage > 100) {
            throw new DomainValidationException("recyclingRatePercentage deve estar entre 0 e 100");
        }
    }
}
