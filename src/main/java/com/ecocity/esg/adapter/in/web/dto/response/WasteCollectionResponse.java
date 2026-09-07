package com.ecocity.esg.adapter.in.web.dto.response;

import com.ecocity.esg.domain.model.WasteType;
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
public class WasteCollectionResponse {

    private String id;
    private String district;
    private WasteType wasteType;
    private double weightKg;
    private double recyclingRatePercentage;
    private Instant collectionDate;
    private String collectorTeam;
    private boolean properlyDisposed;
}
