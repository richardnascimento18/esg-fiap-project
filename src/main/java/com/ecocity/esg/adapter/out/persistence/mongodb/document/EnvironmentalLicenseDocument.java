package com.ecocity.esg.adapter.out.persistence.mongodb.document;

import com.ecocity.esg.domain.model.LicenseStatus;
import com.ecocity.esg.domain.model.LicenseType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.Map;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "environmental_license")
public class EnvironmentalLicenseDocument {

    @Id
    private String id;
    private String licenseNumber;
    private String facility;
    private LicenseType licenseType;
    private LicenseStatus status;
    private Instant issueDate;
    private Instant expirationDate;
    private String issuingAuthority;
    private Map<String, Object> additionalRequirements;
}
