package com.ecocity.esg.adapter.in.scheduler;

import com.ecocity.esg.application.port.in.CheckLicenseExpirationUseCase;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@EnableScheduling
public class LicenseExpirationAlertScheduler {

    private final CheckLicenseExpirationUseCase useCase;

    public LicenseExpirationAlertScheduler(CheckLicenseExpirationUseCase useCase) {
        this.useCase = useCase;
    }

    @Scheduled(cron = "${app.license-alert.cron:0 0 6 * * *}")
    public void alertLicensesCloseToExpiration() {
        useCase.checkExpiringLicenses();
    }
}
