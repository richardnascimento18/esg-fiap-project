package com.ecocity.esg.adapter.in.web.mapper;

import com.ecocity.esg.adapter.in.web.dto.request.WasteCollectionRequest;
import com.ecocity.esg.adapter.in.web.dto.response.WasteCollectionResponse;
import com.ecocity.esg.domain.model.WasteCollection;
import org.springframework.stereotype.Component;

@Component
public class WasteCollectionWebMapper {

    public WasteCollection toDomain(WasteCollectionRequest request) {
        return WasteCollection.builder()
                .district(request.getDistrict())
                .wasteType(request.getWasteType())
                .weightKg(request.getWeightKg())
                .recyclingRatePercentage(request.getRecyclingRatePercentage())
                .collectionDate(request.getCollectionDate())
                .collectorTeam(request.getCollectorTeam())
                .properlyDisposed(request.isProperlyDisposed())
                .build();
    }

    public WasteCollectionResponse toResponse(WasteCollection domain) {
        return WasteCollectionResponse.builder()
                .id(domain.getId())
                .district(domain.getDistrict())
                .wasteType(domain.getWasteType())
                .weightKg(domain.getWeightKg())
                .recyclingRatePercentage(domain.getRecyclingRatePercentage())
                .collectionDate(domain.getCollectionDate())
                .collectorTeam(domain.getCollectorTeam())
                .properlyDisposed(domain.isProperlyDisposed())
                .build();
    }
}
