package com.ecocity.esg.adapter.out.persistence.mongodb.mapper;

import com.ecocity.esg.adapter.out.persistence.mongodb.document.WasteCollectionDocument;
import com.ecocity.esg.domain.model.WasteCollection;
import org.springframework.stereotype.Component;

@Component
public class WasteCollectionPersistenceMapper {

    public WasteCollectionDocument toDocument(WasteCollection domain) {
        return WasteCollectionDocument.builder()
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

    public WasteCollection toDomain(WasteCollectionDocument document) {
        return WasteCollection.builder()
                .id(document.getId())
                .district(document.getDistrict())
                .wasteType(document.getWasteType())
                .weightKg(document.getWeightKg())
                .recyclingRatePercentage(document.getRecyclingRatePercentage())
                .collectionDate(document.getCollectionDate())
                .collectorTeam(document.getCollectorTeam())
                .properlyDisposed(document.isProperlyDisposed())
                .build();
    }
}
