package com.ecocity.esg.adapter.in.web.dto.request;

import com.ecocity.esg.domain.model.LicenseType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.Map;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EnvironmentalLicenseRequest {

    @NotBlank
    private String licenseNumber;

    @NotBlank
    private String facility;

    @NotNull
    private LicenseType licenseType;

    @NotNull
    private Instant issueDate;

    @NotNull
    private Instant expirationDate;

    @NotBlank
    private String issuingAuthority;

    private Map<String, Object> additionalRequirements;
}
