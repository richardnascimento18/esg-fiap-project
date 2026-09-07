package com.ecocity.esg.application.usecase;

import com.ecocity.esg.application.port.in.CheckLicenseExpirationUseCase;
import com.ecocity.esg.application.port.out.EnvironmentalLicenseRepositoryPort;
import com.ecocity.esg.application.port.out.LicenseRenewalAlertPort;
import com.ecocity.esg.domain.model.EnvironmentalLicense;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.List;

public class CheckLicenseExpirationService implements CheckLicenseExpirationUseCase {

    private static final Duration RENEWAL_WINDOW = Duration.ofDays(30);
    private static final int PAGE_SIZE = 100;

    private final EnvironmentalLicenseRepositoryPort repository;
    private final LicenseRenewalAlertPort alerts;
    private final Clock clock;

    public CheckLicenseExpirationService(EnvironmentalLicenseRepositoryPort repository,
                                        LicenseRenewalAlertPort alerts, Clock clock) {
        this.repository = repository;
        this.alerts = alerts;
        this.clock = clock;
    }

    @Override
    public void checkExpiringLicenses() {
        Instant deadline = clock.instant().plus(RENEWAL_WINDOW);
        int page = 0;
        List<EnvironmentalLicense> licenses;
        do {
            licenses = repository.findAll(page++, PAGE_SIZE);
            licenses.stream()
                    .filter(license -> license.requiresRenewalBefore(deadline))
                    .forEach(alerts::notifyRenewalRequired);
        } while (licenses.size() == PAGE_SIZE);
    }
}
