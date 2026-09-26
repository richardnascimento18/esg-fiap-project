package com.ecocity.esg.domain.model;

import com.ecocity.esg.domain.exception.DomainValidationException;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;

class DomainRulesTest {

    private static final Instant NOW = Instant.parse("2026-09-07T12:00:00Z");


    private static EnergyConsumption.EnergyConsumptionBuilder energy() {
        return EnergyConsumption.builder().facilityId("F1").facilityName("Facility").city("City")
                .sourceType(EnergySourceType.SOLAR).readingTimestamp(NOW);
    }

    private static CarbonEmission.CarbonEmissionBuilder carbon() {
        return CarbonEmission.builder().sourceFacility("Facility").emissionType(EmissionType.CO2)
                .reportingPeriod(ReportingQuarter.parse("2026-Q3")).auditedBy("Auditor");
    }

    private static EnvironmentalLicense.EnvironmentalLicenseBuilder license() {
        return EnvironmentalLicense.builder().licenseNumber("LIC-1").facility("Facility")
                .licenseType(LicenseType.OPERATION).issuingAuthority("Authority");
    }

    private static WasteCollection.WasteCollectionBuilder waste() {
        return WasteCollection.builder().district("Centro").wasteType(WasteType.RECYCLABLE)
                .collectionDate(NOW).collectorTeam("Team");
    }

    private static DiversityReport.DiversityReportBuilder diversity() {
        return DiversityReport.builder().department("People");
    }

    @Test
    void energyEqualityDoesNotTriggerAnAlertAndUpdatesPreserveIdentity() {
        EnergyConsumption existing = energy().id("stored").consumptionKwh(1).thresholdKwh(10).build();
        EnergyConsumption replacement = energy().id("incoming").consumptionKwh(10)
                .thresholdKwh(10).alertTriggered(true).build();
        EnergyConsumption updated = existing.updateWith(replacement);
        assertThat(updated.getId()).isEqualTo("stored");
        assertThat(updated.isAlertTriggered()).isFalse();
        assertThat(existing.getConsumptionKwh()).isEqualTo(1);
        assertThat(updated.updateWith(replacement.toBuilder().consumptionKwh(11).build()).isAlertTriggered()).isTrue();
    }

    @Test
    void exactCarbonCompensationIsSufficient() {
        CarbonEmission input = carbon().emissionTonnes(10).compensationTonnes(10).reportingPeriod(ReportingQuarter.parse("2026-Q3")).build();
        assertThat(input.forCreation().isCompensated()).isTrue();
        assertThat(input.updateWith(input.toBuilder().compensationTonnes(9).build()).isCompensated()).isFalse();
    }

    @Test
    void licenseStatusUsesStrictExpirationAndPreservesSuspension() {
        EnvironmentalLicense license = license().issueDate(NOW.minusSeconds(60))
                .expirationDate(NOW).status(LicenseStatus.RENEWAL_IN_PROGRESS).build();
        assertThat(license.forCreation(NOW).getStatus()).isEqualTo(LicenseStatus.RENEWAL_IN_PROGRESS);
        assertThat(license.forCreation(NOW.plusNanos(1)).getStatus()).isEqualTo(LicenseStatus.EXPIRED);
        assertThat(license.toBuilder().status(LicenseStatus.SUSPENDED).build()
                .forCreation(NOW.plusSeconds(1)).getStatus()).isEqualTo(LicenseStatus.SUSPENDED);
        assertThat(license.toBuilder().issueDate(NOW).build().forCreation(NOW).getStatus()).isEqualTo(LicenseStatus.RENEWAL_IN_PROGRESS);
    }

    @Test
    void licenseUpdatesPreserveSuspension() {
        EnvironmentalLicense stored = license().id("stored").status(LicenseStatus.SUSPENDED)
                .issueDate(NOW.minusSeconds(60)).expirationDate(NOW.plusSeconds(60)).build();
        EnvironmentalLicense request = stored.toBuilder().id("incoming").status(null).build();
        EnvironmentalLicense updated = stored.updateWith(request, NOW);
        assertThat(updated.getId()).isEqualTo("stored");
        assertThat(updated.getStatus()).isEqualTo(LicenseStatus.SUSPENDED);
        assertThat(stored.getStatus()).isEqualTo(LicenseStatus.SUSPENDED);
    }

    @Test
    void invalidDatesKeepTheExistingErrorMessage() {
        EnvironmentalLicense invalid = license().issueDate(NOW)
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
        assertThatThrownBy(() -> energy().consumptionKwh(-1).build().forCreation())
                .isInstanceOf(DomainValidationException.class);
        assertThatThrownBy(() -> energy().thresholdKwh(Double.NaN).build().forCreation())
                .isInstanceOf(DomainValidationException.class);
        assertThatThrownBy(() -> carbon().reportingPeriod(ReportingQuarter.parse("2026-Q5")).build().forCreation())
                .isInstanceOf(DomainValidationException.class);
        assertThatThrownBy(() -> waste().weightKg(-1).build().forCreation())
                .isInstanceOf(DomainValidationException.class);
        assertThatThrownBy(() -> diversity().reportingMonth("2026-13").build().forCreation())
                .isInstanceOf(DomainValidationException.class);
    }

    @Test
    void reportingQuarterHasCanonicalValueSemantics() {
        assertThat(ReportingQuarter.parse("2026-Q3")).isEqualTo(new ReportingQuarter(2026, 3));
        assertThat(ReportingQuarter.parse("2026-Q3").toString()).isEqualTo("2026-Q3");
        assertThatThrownBy(() -> ReportingQuarter.parse("2026-Q5"))
                .isInstanceOf(DomainValidationException.class);
        assertThatThrownBy(() -> new ReportingQuarter(2026, 0))
                .isInstanceOf(DomainValidationException.class);
    }

    @Test
    void licenseExpirationIsCalculatedAtReadTime() {
        EnvironmentalLicense stored = license().issueDate(NOW.minusSeconds(60))
                .expirationDate(NOW).status(LicenseStatus.ACTIVE).build();
        assertThat(stored.withEffectiveStatusAt(NOW.plusNanos(1)).getStatus()).isEqualTo(LicenseStatus.EXPIRED);
        assertThat(stored.getStatus()).isEqualTo(LicenseStatus.ACTIVE);
    }
}
