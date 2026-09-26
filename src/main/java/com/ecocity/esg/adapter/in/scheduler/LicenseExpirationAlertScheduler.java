package com.ecocity.esg.adapter.in.scheduler;

import com.ecocity.esg.application.port.in.CheckLicenseExpirationUseCase;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
@EnableScheduling
public class LicenseExpirationAlertScheduler {
    private static final Logger log = LoggerFactory.getLogger(LicenseExpirationAlertScheduler.class);

    private final CheckLicenseExpirationUseCase useCase;

    public LicenseExpirationAlertScheduler(CheckLicenseExpirationUseCase useCase) {
        this.useCase = useCase;
    }

    @Scheduled(cron = "${app.license-alert.cron:0 0 6 * * *}")
    public void alertLicensesCloseToExpiration() {
        try {
            useCase.checkExpiringLicenses();
        } catch (RuntimeException ex) {
            log.error("License renewal scan failed: type={}", ex.getClass().getSimpleName());
            throw ex;
        }
    }
}
