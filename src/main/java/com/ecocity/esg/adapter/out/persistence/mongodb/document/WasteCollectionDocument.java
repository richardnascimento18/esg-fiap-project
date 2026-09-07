package com.ecocity.esg.adapter.out.persistence.mongodb.document;

import com.ecocity.esg.domain.model.WasteType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "waste_collection")
public class WasteCollectionDocument {

    @Id
    private String id;
    private String district;
    private WasteType wasteType;
    private double weightKg;
    private double recyclingRatePercentage;
    private Instant collectionDate;
    private String collectorTeam;
    private boolean properlyDisposed;
}
