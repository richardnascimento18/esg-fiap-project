package com.ecocity.esg.adapter.out.persistence.mongodb.mapper;

import com.ecocity.esg.adapter.out.persistence.mongodb.document.EnvironmentalLicenseDocument;
import com.ecocity.esg.domain.model.EnvironmentalLicense;
import org.springframework.stereotype.Component;

@Component
public class EnvironmentalLicensePersistenceMapper {

    public EnvironmentalLicenseDocument toDocument(EnvironmentalLicense domain) {
        return EnvironmentalLicenseDocument.builder()
                .id(domain.getId())
                .licenseNumber(domain.getLicenseNumber())
                .facility(domain.getFacility())
                .licenseType(domain.getLicenseType())
                .status(domain.getStatus())
                .issueDate(domain.getIssueDate())
                .expirationDate(domain.getExpirationDate())
                .issuingAuthority(domain.getIssuingAuthority())
                .additionalRequirements(domain.getAdditionalRequirements())
                .build();
    }

    public EnvironmentalLicense toDomain(EnvironmentalLicenseDocument document) {
        return EnvironmentalLicense.builder()
                .id(document.getId())
                .licenseNumber(document.getLicenseNumber())
                .facility(document.getFacility())
                .licenseType(document.getLicenseType())
                .status(document.getStatus())
                .issueDate(document.getIssueDate())
                .expirationDate(document.getExpirationDate())
                .issuingAuthority(document.getIssuingAuthority())
                .additionalRequirements(document.getAdditionalRequirements())
                .build();
    }
}
