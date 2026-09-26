package com.ecocity.esg.adapter.out.persistence.mongodb.document;

import com.ecocity.esg.domain.model.EnergySourceType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.Map;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "energy_consumption")
public class EnergyConsumptionDocument {

    @Id
    private String id;
    @Version
    private Long version;
    private String facilityId;
    private String facilityName;
    private String city;
    private EnergySourceType sourceType;
    private double consumptionKwh;
    private double thresholdKwh;
    private boolean alertTriggered;
    private Instant readingTimestamp;
    private Map<String, Object> sensorMetadata;
}
