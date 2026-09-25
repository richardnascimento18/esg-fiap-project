package com.ecocity.esg.domain.model;

import com.ecocity.esg.domain.exception.DomainValidationException;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;

class DomainRulesTest {

    private static final Instant NOW = Instant.parse("2026-09-07T12:00:00Z");

    @Test
    void energyEqualityDoesNotTriggerAnAlertAndUpdatesPreserveIdentity() {
        EnergyConsumption existing = EnergyConsumption.builder().id("stored").consumptionKwh(1).thresholdKwh(10).build();
        EnergyConsumption replacement = EnergyConsumption.builder().id("incoming").consumptionKwh(10)
                .thresholdKwh(10).alertTriggered(true).build();
        EnergyConsumption updated = existing.updateWith(replacement);
        assertThat(updated.getId()).isEqualTo("stored");
        assertThat(updated.isAlertTriggered()).isFalse();
        assertThat(existing.getConsumptionKwh()).isEqualTo(1);
        assertThat(updated.updateWith(replacement.toBuilder().consumptionKwh(11).build()).isAlertTriggered()).isTrue();
    }

    @Test
    void exactCarbonCompensationIsSufficient() {
        CarbonEmission input = CarbonEmission.builder().emissionTonnes(10).compensationTonnes(10).reportingPeriod("2026-Q3").build();
        assertThat(input.forCreation().isCompensated()).isTrue();
        assertThat(input.updateWith(input.toBuilder().compensationTonnes(9).build()).isCompensated()).isFalse();
    }

    @Test
    void licenseStatusUsesStrictExpirationAndPreservesSuspension() {
        EnvironmentalLicense license = EnvironmentalLicense.builder().issueDate(NOW.minusSeconds(60))
                .expirationDate(NOW).status(LicenseStatus.RENEWAL_IN_PROGRESS).build();
        assertThat(license.forCreation(NOW).getStatus()).isEqualTo(LicenseStatus.RENEWAL_IN_PROGRESS);
        assertThat(license.forCreation(NOW.plusNanos(1)).getStatus()).isEqualTo(LicenseStatus.EXPIRED);
        assertThat(license.toBuilder().status(LicenseStatus.SUSPENDED).build()
                .forCreation(NOW.plusSeconds(1)).getStatus()).isEqualTo(LicenseStatus.SUSPENDED);
        assertThat(license.toBuilder().issueDate(NOW).build().forCreation(NOW).getStatus()).isEqualTo(LicenseStatus.RENEWAL_IN_PROGRESS);
    }

    @Test
    void licenseUpdatesPreserveSuspension() {
        EnvironmentalLicense stored = EnvironmentalLicense.builder().id("stored").status(LicenseStatus.SUSPENDED)
                .issueDate(NOW.minusSeconds(60)).expirationDate(NOW.plusSeconds(60)).build();
        EnvironmentalLicense request = stored.toBuilder().id("incoming").status(null).build();
        EnvironmentalLicense updated = stored.updateWith(request, NOW);
        assertThat(updated.getId()).isEqualTo("stored");
        assertThat(updated.getStatus()).isEqualTo(LicenseStatus.SUSPENDED);
        assertThat(stored.getStatus()).isEqualTo(LicenseStatus.SUSPENDED);
    }

    @Test
    void invalidDatesKeepTheExistingErrorMessage() {
        EnvironmentalLicense invalid = EnvironmentalLicense.builder().issueDate(NOW)
                .expirationDate(NOW.minusSeconds(1)).build();
        assertThatThrownBy(() -> invalid.forCreation(NOW)).isInstanceOf(DomainValidationException.class)
                .hasMessage("expirationDate nao pode ser anterior a issueDate");
    }

    @Test
    void copyingMetadataPreservesNullValuesAndInsertionOrder() {
        Map<String, Object> metadata = new LinkedHashMap<>();
        metadata.put("voltage", 220);
        metadata.put("optional", null);
        EnergyConsumption energy = EnergyConsumption.builder().sensorMetadata(metadata).build();
        metadata.put("voltage", 110);
        assertThat(energy.getSensorMetadata()).containsEntry("voltage", 220).containsEntry("optional", null);
        assertThat(energy.getSensorMetadata().keySet()).containsExactly("voltage", "optional");
        assertThatThrownBy(() -> energy.getSensorMetadata().put("voltage", 110))
                .isInstanceOf(UnsupportedOperationException.class);
        assertThat(EnergyConsumption.builder().build().getSensorMetadata()).isNull();
    }

    @Test
    void measurementsAndReportingPeriodsAreValidatedWithoutHttp() {
        assertThatThrownBy(() -> EnergyConsumption.builder().consumptionKwh(-1).build().forCreation())
                .isInstanceOf(DomainValidationException.class);
        assertThatThrownBy(() -> EnergyConsumption.builder().thresholdKwh(Double.NaN).build().forCreation())
                .isInstanceOf(DomainValidationException.class);
        assertThatThrownBy(() -> CarbonEmission.builder().reportingPeriod("2026-Q5").build().forCreation())
                .isInstanceOf(DomainValidationException.class);
        assertThatThrownBy(() -> WasteCollection.builder().weightKg(-1).build().forCreation())
                .isInstanceOf(DomainValidationException.class);
        assertThatThrownBy(() -> DiversityReport.builder().reportingMonth("2026-13").build().forCreation())
                .isInstanceOf(DomainValidationException.class);
    }

    @Test
    void licenseExpirationIsCalculatedAtReadTime() {
        EnvironmentalLicense stored = EnvironmentalLicense.builder().issueDate(NOW.minusSeconds(60))
                .expirationDate(NOW).status(LicenseStatus.ACTIVE).build();
        assertThat(stored.withEffectiveStatusAt(NOW.plusNanos(1)).getStatus()).isEqualTo(LicenseStatus.EXPIRED);
        assertThat(stored.getStatus()).isEqualTo(LicenseStatus.ACTIVE);
    }
}
