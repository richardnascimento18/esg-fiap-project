package com.ecocity.esg.infrastructure.config;

import com.ecocity.esg.application.port.in.EnergyConsumptionUseCase;
import com.ecocity.esg.application.port.out.EnergyConsumptionRepositoryPort;
import com.ecocity.esg.application.usecase.EnergyConsumptionService;
import com.ecocity.esg.application.port.in.WasteCollectionUseCase;
import com.ecocity.esg.application.port.out.WasteCollectionRepositoryPort;
import com.ecocity.esg.application.usecase.WasteCollectionService;
import com.ecocity.esg.application.port.in.CarbonEmissionUseCase;
import com.ecocity.esg.application.port.out.CarbonEmissionRepositoryPort;
import com.ecocity.esg.application.usecase.CarbonEmissionService;
import com.ecocity.esg.application.port.in.DiversityReportUseCase;
import com.ecocity.esg.application.port.out.DiversityReportRepositoryPort;
import com.ecocity.esg.application.usecase.DiversityReportService;
import com.ecocity.esg.application.port.in.EnvironmentalLicenseUseCase;
import com.ecocity.esg.application.port.out.EnvironmentalLicenseRepositoryPort;
import com.ecocity.esg.application.usecase.EnvironmentalLicenseService;
import com.ecocity.esg.application.port.in.CheckLicenseExpirationUseCase;
import com.ecocity.esg.application.port.out.LicenseRenewalAlertPort;
import com.ecocity.esg.application.usecase.CheckLicenseExpirationService;
import com.ecocity.esg.application.usecase.CoordinatedLicenseExpirationService;
import com.ecocity.esg.application.port.out.LicenseScanCoordinationPort;
import com.ecocity.esg.application.port.in.IdempotencyUseCase;
import com.ecocity.esg.application.port.out.IdempotencyReservationPort;
import com.ecocity.esg.application.usecase.IdempotencyService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

@Configuration(proxyBeanMethods = false)
public class ApplicationConfiguration {

    @Bean
    public IdempotencyUseCase idempotencyUseCase(IdempotencyReservationPort reservations) {
        return new IdempotencyService(reservations);
    }

    @Bean
    public Clock clock() {
        return Clock.systemUTC();
    }

    @Bean
    public EnergyConsumptionUseCase energyConsumptionUseCase(EnergyConsumptionRepositoryPort repository) {
        return new EnergyConsumptionService(repository);
    }

    @Bean
    public WasteCollectionUseCase wasteCollectionUseCase(WasteCollectionRepositoryPort repository) {
        return new WasteCollectionService(repository);
    }

    @Bean
    public CarbonEmissionUseCase carbonEmissionUseCase(CarbonEmissionRepositoryPort repository) {
        return new CarbonEmissionService(repository);
    }

    @Bean
    public DiversityReportUseCase diversityReportUseCase(DiversityReportRepositoryPort repository) {
        return new DiversityReportService(repository);
    }

    @Bean
    public EnvironmentalLicenseUseCase environmentalLicenseUseCase(EnvironmentalLicenseRepositoryPort repository, Clock clock) {
        return new EnvironmentalLicenseService(repository, clock);
    }

    @Bean
    public CheckLicenseExpirationUseCase checkLicenseExpirationUseCase(
            EnvironmentalLicenseRepositoryPort repository, LicenseRenewalAlertPort alerts, Clock clock,
            LicenseScanCoordinationPort coordination) {
        return new CoordinatedLicenseExpirationService(
                new CheckLicenseExpirationService(repository, alerts, clock), coordination);
    }
}
