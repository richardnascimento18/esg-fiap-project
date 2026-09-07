package com.ecocity.esg.application.usecase;

import com.ecocity.esg.application.port.out.EnvironmentalLicenseRepositoryPort;
import com.ecocity.esg.application.port.out.LicenseRenewalAlertPort;
import com.ecocity.esg.domain.model.EnvironmentalLicense;
import com.ecocity.esg.domain.model.LicenseStatus;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.*;

class CheckLicenseExpirationServiceTest {

    private static final Instant NOW = Instant.parse("2026-09-07T12:00:00Z");
    private final EnvironmentalLicenseRepositoryPort repository = mock(EnvironmentalLicenseRepositoryPort.class);
    private final LicenseRenewalAlertPort alerts = mock(LicenseRenewalAlertPort.class);
    private final CheckLicenseExpirationService service = new CheckLicenseExpirationService(
            repository, alerts, Clock.fixed(NOW, ZoneOffset.UTC));

    private EnvironmentalLicense license(Instant expiration, LicenseStatus status) {
        return EnvironmentalLicense.builder().expirationDate(expiration).status(status).build();
    }

    @Test
    void preservesStrictDeadlineAndIncludesAlreadyExpiredLicenses() {
        Instant deadline = NOW.plus(Duration.ofDays(30));
        EnvironmentalLicense expired = license(NOW.minusSeconds(1), LicenseStatus.EXPIRED);
        EnvironmentalLicense soon = license(deadline.minusNanos(1), LicenseStatus.RENEWAL_IN_PROGRESS);
        EnvironmentalLicense boundary = license(deadline, LicenseStatus.ACTIVE);
        EnvironmentalLicense suspended = license(NOW, LicenseStatus.SUSPENDED);
        EnvironmentalLicense later = license(deadline.plusSeconds(1), LicenseStatus.ACTIVE);
        when(repository.findAll(0, 100)).thenReturn(List.of(expired, soon, boundary, suspended, later));

        service.checkExpiringLicenses();

        verify(alerts).notifyRenewalRequired(expired);
        verify(alerts).notifyRenewalRequired(soon);
        verifyNoMoreInteractions(alerts);
        verify(repository).findAll(0, 100);
        verifyNoMoreInteractions(repository);
    }

    @Test
    void processesAllPagesWithoutWritingLicenseStatus() {
        EnvironmentalLicense far = license(NOW.plus(Duration.ofDays(90)), LicenseStatus.ACTIVE);
        EnvironmentalLicense due = license(NOW, LicenseStatus.ACTIVE);
        when(repository.findAll(0, 100)).thenReturn(Collections.nCopies(100, far));
        when(repository.findAll(1, 100)).thenReturn(List.of(due));

        service.checkExpiringLicenses();

        verify(repository).findAll(0, 100);
        verify(repository).findAll(1, 100);
        verifyNoMoreInteractions(repository);
        verify(alerts).notifyRenewalRequired(due);
        verifyNoMoreInteractions(alerts);
    }

    @Test
    void handlesAnEmptyDatabase() {
        when(repository.findAll(0, 100)).thenReturn(List.of());
        service.checkExpiringLicenses();
        verifyNoInteractions(alerts);
        verify(repository).findAll(0, 100);
        verifyNoMoreInteractions(repository);
    }
}
