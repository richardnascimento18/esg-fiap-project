package com.ecocity.esg.domain.model;

import com.ecocity.esg.domain.exception.DomainValidationException;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Governance pillar: controls environmental licenses, their validity period and
 * the extra requirements demanded by the issuing authority (which vary per license type,
 * again exercising MongoDB's flexible document structure).
 */
@Getter
public final class EnvironmentalLicense {

    private final String id;
    private final String licenseNumber;
    private final String facility;
    private final LicenseType licenseType;
    private final LicenseStatus status;
    private final Instant issueDate;
    private final Instant expirationDate;
    private final String issuingAuthority;
    private final Map<String, Object> additionalRequirements;

    // The builder also restores stored state; write operations below apply business rules.
    @Builder(toBuilder = true)
    private EnvironmentalLicense(String id,
            String licenseNumber,
            String facility,
            LicenseType licenseType,
            LicenseStatus status,
            Instant issueDate,
            Instant expirationDate,
            String issuingAuthority,
            Map<String, Object> additionalRequirements) {
        this.id = id;
        this.licenseNumber = licenseNumber;
        this.facility = facility;
        this.licenseType = licenseType;
        this.status = status;
        this.issueDate = issueDate;
        this.expirationDate = expirationDate;
        this.issuingAuthority = issuingAuthority;
        this.additionalRequirements = additionalRequirements == null
                ? null : Collections.unmodifiableMap(new LinkedHashMap<>(additionalRequirements));
    }

    public EnvironmentalLicense forCreation(Instant now) {
        validate();
        return toBuilder().id(null).status(statusAt(now)).build();
    }

    public EnvironmentalLicense updateWith(EnvironmentalLicense replacement, Instant now) {
        replacement.validate();
        return replacement.toBuilder().id(id).status(replacement.statusAt(now)).build();
    }

    public void validate() {
        if (expirationDate.isBefore(issueDate)) {
            throw new DomainValidationException("expirationDate nao pode ser anterior a issueDate");
        }
    }

    private LicenseStatus statusAt(Instant now) {
        if (status == LicenseStatus.SUSPENDED) {
            return LicenseStatus.SUSPENDED;
        }
        return now.isAfter(expirationDate) ? LicenseStatus.EXPIRED : LicenseStatus.ACTIVE;
    }

    public boolean requiresRenewalBefore(Instant deadline) {
        return status != LicenseStatus.SUSPENDED && expirationDate.isBefore(deadline);
    }
}
