package com.ecocity.esg.adapter.out.notification;

import com.ecocity.esg.application.port.out.LicenseRenewalAlertPort;
import com.ecocity.esg.domain.model.EnvironmentalLicense;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class LoggingLicenseRenewalAlertAdapter implements LicenseRenewalAlertPort {

    private static final Logger log = LoggerFactory.getLogger(LoggingLicenseRenewalAlertAdapter.class);

    @Override
    public void notifyRenewalRequired(EnvironmentalLicense license) {
        log.warn("Licenca {} da instalacao {} vence em {} - iniciar processo de renovacao",
                license.getLicenseNumber(), license.getFacility(), license.getExpirationDate());
    }
}
