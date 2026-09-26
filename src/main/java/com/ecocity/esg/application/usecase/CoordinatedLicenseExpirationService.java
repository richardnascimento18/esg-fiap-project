package com.ecocity.esg.application.usecase;

import com.ecocity.esg.application.port.in.CheckLicenseExpirationUseCase;
import com.ecocity.esg.application.port.out.LicenseScanCoordinationPort;

public class CoordinatedLicenseExpirationService implements CheckLicenseExpirationUseCase {
    private final CheckLicenseExpirationUseCase scan;
    private final LicenseScanCoordinationPort coordination;

    public CoordinatedLicenseExpirationService(CheckLicenseExpirationUseCase scan,
                                               LicenseScanCoordinationPort coordination) {
        this.scan = scan;
        this.coordination = coordination;
    }

    @Override
    public void checkExpiringLicenses() {
        coordination.runIfLeader(scan::checkExpiringLicenses);
    }
}
