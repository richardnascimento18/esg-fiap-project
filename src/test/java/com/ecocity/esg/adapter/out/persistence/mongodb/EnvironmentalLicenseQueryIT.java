package com.ecocity.esg.adapter.out.persistence.mongodb;

import com.ecocity.esg.application.port.out.EnvironmentalLicenseRepositoryPort;
import com.ecocity.esg.domain.model.EnvironmentalLicense;
import com.ecocity.esg.domain.model.LicenseStatus;
import com.ecocity.esg.domain.model.LicenseType;
import com.ecocity.esg.support.MongoIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class EnvironmentalLicenseQueryIT extends MongoIntegrationTest {
    @Autowired private EnvironmentalLicenseRepositoryPort repository;

    @Test
    void renewalQueryFiltersAtDatabase() {
        String prefix = "QUERY-" + UUID.randomUUID();
        Instant deadline = Instant.parse("2026-10-01T00:00:00Z");
        save(prefix + "-A", deadline.minusSeconds(1), LicenseStatus.ACTIVE);
        save(prefix + "-B", deadline.minusSeconds(1), LicenseStatus.RENEWAL_IN_PROGRESS);
        save(prefix + "-C", deadline.minusSeconds(1), LicenseStatus.SUSPENDED);
        save(prefix + "-D", deadline, LicenseStatus.ACTIVE);

        var candidates = repository.findRenewalCandidatesBefore(deadline, 0, 100).stream()
                .filter(license -> license.getLicenseNumber().startsWith(prefix)).toList();
        assertThat(candidates).extracting(EnvironmentalLicense::getLicenseNumber)
                .containsExactlyInAnyOrder(prefix + "-A", prefix + "-B");
    }

    private void save(String number, Instant expiration, LicenseStatus status) {
        repository.save(EnvironmentalLicense.builder()
                .licenseNumber(number).facility("Test facility").licenseType(LicenseType.OPERATION)
                .issueDate(Instant.parse("2026-01-01T00:00:00Z"))
                .expirationDate(expiration).issuingAuthority("Test authority").status(status).build());
    }
}
