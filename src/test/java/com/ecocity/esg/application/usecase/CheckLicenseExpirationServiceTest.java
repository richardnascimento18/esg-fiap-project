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
    void alertsOnlyUpcomingCandidates() {
        Instant deadline = NOW.plus(Duration.ofDays(30));
        EnvironmentalLicense dueNow = license(NOW, LicenseStatus.ACTIVE);
        EnvironmentalLicense soon = license(deadline, LicenseStatus.ACTIVE);
        when(repository.findRenewalCandidatesBetween(NOW, deadline, 0, 100)).thenReturn(List.of(dueNow, soon));

        service.checkExpiringLicenses();

        verify(alerts).notifyRenewalRequired(dueNow);
        verify(alerts).notifyRenewalRequired(soon);
        verifyNoMoreInteractions(alerts);
        verify(repository).findRenewalCandidatesBetween(NOW, deadline, 0, 100);
        verifyNoMoreInteractions(repository);
    }

    @Test
    void processesAllPagesWithoutWritingLicenseStatus() {
        Instant deadline = NOW.plus(Duration.ofDays(30));
        EnvironmentalLicense due = license(NOW, LicenseStatus.ACTIVE);
        EnvironmentalLicense later = license(NOW.plusSeconds(1), LicenseStatus.ACTIVE);
        when(repository.findRenewalCandidatesBetween(NOW, deadline, 0, 100)).thenReturn(Collections.nCopies(100, due));
        when(repository.findRenewalCandidatesBetween(NOW, deadline, 1, 100)).thenReturn(List.of(later));

        service.checkExpiringLicenses();

        verify(repository).findRenewalCandidatesBetween(NOW, deadline, 0, 100);
        verify(repository).findRenewalCandidatesBetween(NOW, deadline, 1, 100);
        verifyNoMoreInteractions(repository);
        verify(alerts, times(100)).notifyRenewalRequired(due);
        verify(alerts).notifyRenewalRequired(later);
        verifyNoMoreInteractions(alerts);
    }

    @Test
    void handlesAnEmptyDatabase() {
        Instant deadline = NOW.plus(Duration.ofDays(30));
        when(repository.findRenewalCandidatesBetween(NOW, deadline, 0, 100)).thenReturn(List.of());
        service.checkExpiringLicenses();
        verifyNoInteractions(alerts);
        verify(repository).findRenewalCandidatesBetween(NOW, deadline, 0, 100);
        verifyNoMoreInteractions(repository);
    }
}
