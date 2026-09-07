package com.ecocity.esg.adapter.in.web.dto.request;

import com.ecocity.esg.domain.model.WasteType;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WasteCollectionRequest {

    @NotBlank
    private String district;

    @NotNull
    private WasteType wasteType;

    @PositiveOrZero
    private double weightKg;

    @DecimalMin("0.0")
    @DecimalMax("100.0")
    private double recyclingRatePercentage;

    @NotNull
    private Instant collectionDate;

    @NotBlank
    private String collectorTeam;

    private boolean properlyDisposed;
}
