package com.ecocity.esg.adapter.in.web.mapper;

import com.ecocity.esg.adapter.in.web.dto.request.EnvironmentalLicenseRequest;
import com.ecocity.esg.adapter.in.web.dto.response.EnvironmentalLicenseResponse;
import com.ecocity.esg.domain.model.EnvironmentalLicense;
import org.springframework.stereotype.Component;

@Component
public class EnvironmentalLicenseWebMapper {

    public EnvironmentalLicense toDomain(EnvironmentalLicenseRequest request) {
        return EnvironmentalLicense.builder()
                .licenseNumber(request.getLicenseNumber())
                .facility(request.getFacility())
                .licenseType(request.getLicenseType())
                .issueDate(request.getIssueDate())
                .expirationDate(request.getExpirationDate())
                .issuingAuthority(request.getIssuingAuthority())
                .additionalRequirements(request.getAdditionalRequirements())
                .build();
    }

    public EnvironmentalLicenseResponse toResponse(EnvironmentalLicense domain) {
        return EnvironmentalLicenseResponse.builder()
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
}
