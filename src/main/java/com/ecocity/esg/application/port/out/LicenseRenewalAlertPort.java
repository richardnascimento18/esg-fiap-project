package com.ecocity.esg.application.port.out;

import com.ecocity.esg.domain.model.EnvironmentalLicense;

public interface LicenseRenewalAlertPort {
    void notifyRenewalRequired(EnvironmentalLicense license);
}
