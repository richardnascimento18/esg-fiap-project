package com.ecocity.esg.adapter.in.bootstrap;

import com.ecocity.esg.domain.model.CarbonEmission;
import com.ecocity.esg.domain.model.DiversityReport;
import com.ecocity.esg.domain.model.EnergyConsumption;
import com.ecocity.esg.domain.model.EnvironmentalLicense;
import com.ecocity.esg.domain.model.WasteCollection;
import com.ecocity.esg.application.port.in.CarbonEmissionUseCase;
import com.ecocity.esg.application.port.in.DiversityReportUseCase;
import com.ecocity.esg.application.port.in.EnergyConsumptionUseCase;
import com.ecocity.esg.application.port.in.EnvironmentalLicenseUseCase;
import com.ecocity.esg.application.port.in.WasteCollectionUseCase;
import com.ecocity.esg.domain.model.EmissionType;
import com.ecocity.esg.domain.model.EnergySourceType;
import com.ecocity.esg.domain.model.LicenseStatus;
import com.ecocity.esg.domain.model.LicenseType;
import com.ecocity.esg.domain.model.WasteType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.Clock;
import java.time.temporal.ChronoUnit;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Populates each of the 5 collections with at least 10 sample documents so the
 * application is demonstrable right after startup, as requested by the assignment.
 * It only runs for the "dev" profile and is idempotent: it skips a collection that
 * already has data, so restarting the container never creates duplicates.
 */
@Component
@Profile("dev & !staging & !prod & !production")
@ConditionalOnProperty(name = "app.seed-demo-data", havingValue = "true")
@Order(2)
public class DataSeeder implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);
    private static final int SAMPLE_SIZE = 12;

    private final Clock clock;

    private final EnergyConsumptionUseCase energyUseCase;
    private final WasteCollectionUseCase wasteUseCase;
    private final CarbonEmissionUseCase carbonUseCase;
    private final DiversityReportUseCase diversityUseCase;
    private final EnvironmentalLicenseUseCase licenseUseCase;

    public DataSeeder(EnergyConsumptionUseCase energyUseCase,
                       WasteCollectionUseCase wasteUseCase,
                       CarbonEmissionUseCase carbonUseCase,
                       DiversityReportUseCase diversityUseCase,
                       EnvironmentalLicenseUseCase licenseUseCase, Clock clock) {
        this.clock = clock;
        this.energyUseCase = energyUseCase;
        this.wasteUseCase = wasteUseCase;
        this.carbonUseCase = carbonUseCase;
        this.diversityUseCase = diversityUseCase;
        this.licenseUseCase = licenseUseCase;
    }

    @Override
    public void run(ApplicationArguments args) {
        seedEnergyConsumption();
        seedWasteCollection();
        seedCarbonEmission();
        seedDiversityReport();
        seedEnvironmentalLicense();
    }

    private void seedEnergyConsumption() {
        if (!energyUseCase.findAll(0, 1).isEmpty()) {
            return;
        }
        String[] facilities = {"Paco Municipal", "UBS Centro", "Escola Vila Nova", "Terminal Rodoviario",
                "Parque Solar Norte", "Reservatorio Leste", "Biblioteca Publica", "CEU das Artes",
                "Ginasio Municipal", "Aterro Sanitario", "Praca Central", "Estacao de Tratamento"};
        EnergySourceType[] sources = EnergySourceType.values();
        for (int i = 0; i < SAMPLE_SIZE; i++) {
            EnergySourceType sourceType = sources[i % sources.length];
            double consumption = 500 + (i * 137.5);
            double threshold = 3000;
            Map<String, Object> metadata = new LinkedHashMap<>();
            if (sourceType == EnergySourceType.SOLAR) {
                metadata.put("panelEfficiencyPercentage", 18.5 + i);
                metadata.put("panelCount", 40 + i);
            } else if (sourceType == EnergySourceType.WIND) {
                metadata.put("averageWindSpeedKmh", 22.0 + i);
                metadata.put("turbineCount", 3);
            } else {
                metadata.put("voltage", 220);
                metadata.put("phase", i % 3 == 0 ? "trifasico" : "monofasico");
            }
            EnergyConsumption sample = EnergyConsumption.builder()
                    .facilityId("FAC-%03d".formatted(i + 1))
                    .facilityName(facilities[i % facilities.length])
                    .city("Sao Paulo")
                    .sourceType(sourceType)
                    .consumptionKwh(consumption)
                    .thresholdKwh(threshold)
                    .alertTriggered(consumption > threshold)
                    .readingTimestamp(clock.instant().minus(i, ChronoUnit.DAYS))
                    .sensorMetadata(metadata)
                    .build();
            energyUseCase.create(sample);
        }
        log.info("Seed: {} documentos inseridos em energy_consumption", SAMPLE_SIZE);
    }

    private void seedWasteCollection() {
        if (!wasteUseCase.findAll(0, 1).isEmpty()) {
            return;
        }
        String[] districts = {"Centro", "Zona Norte", "Zona Sul", "Zona Leste", "Zona Oeste"};
        WasteType[] types = WasteType.values();
        for (int i = 0; i < SAMPLE_SIZE; i++) {
            WasteType wasteType = types[i % types.length];
            double rate = wasteType == WasteType.RECYCLABLE ? 70 + i : 20 + i;
            WasteCollection sample = WasteCollection.builder()
                    .district(districts[i % districts.length])
                    .wasteType(wasteType)
                    .weightKg(150.0 + (i * 22.3))
                    .recyclingRatePercentage(Math.min(rate, 100))
                    .collectionDate(clock.instant().minus(i, ChronoUnit.DAYS))
                    .collectorTeam("Equipe-%02d".formatted((i % 4) + 1))
                    .properlyDisposed(i % 5 != 0)
                    .build();
            wasteUseCase.create(sample);
        }
        log.info("Seed: {} documentos inseridos em waste_collection", SAMPLE_SIZE);
    }

    private void seedCarbonEmission() {
        if (!carbonUseCase.findAll(0, 1).isEmpty()) {
            return;
        }
        String[] facilities = {"Terminal Rodoviario", "Aterro Sanitario", "Frota Municipal",
                "Usina de Compostagem", "Central de Distribuicao"};
        EmissionType[] types = EmissionType.values();
        for (int i = 0; i < SAMPLE_SIZE; i++) {
            double emission = 40.0 + (i * 5.5);
            double compensation = i % 3 == 0 ? emission - 5 : emission + 2;
            CarbonEmission sample = CarbonEmission.builder()
                    .sourceFacility(facilities[i % facilities.length])
                    .emissionType(types[i % types.length])
                    .emissionTonnes(emission)
                    .compensationTonnes(Math.max(compensation, 0))
                    .compensated(compensation >= emission)
                    .reportingPeriod("2026-Q%d".formatted((i % 4) + 1))
                    .auditedBy("Auditoria Ambiental Municipal")
                    .build();
            carbonUseCase.create(sample);
        }
        log.info("Seed: {} documentos inseridos em carbon_emission", SAMPLE_SIZE);
    }

    private void seedDiversityReport() {
        if (!diversityUseCase.findAll(0, 1).isEmpty()) {
            return;
        }
        String[] departments = {"Tecnologia", "Obras", "Saude", "Educacao", "Administracao",
                "Meio Ambiente", "Assistencia Social", "Financas", "Recursos Humanos", "Seguranca",
                "Cultura", "Planejamento"};
        for (int i = 0; i < SAMPLE_SIZE; i++) {
            DiversityReport sample = DiversityReport.builder()
                    .department(departments[i % departments.length])
                    .totalEmployees(20 + (i * 3))
                    .womenPercentage(35.0 + i)
                    .blackAndMixedRacePercentage(30.0 + i)
                    .personsWithDisabilitiesPercentage(3.0 + (i % 5))
                    .lgbtqiaPercentage(4.0 + (i % 6))
                    .reportingMonth("2026-%02d".formatted((i % 9) + 1))
                    .diversityTrainingCompleted(i % 4 != 0)
                    .build();
            diversityUseCase.create(sample);
        }
        log.info("Seed: {} documentos inseridos em diversity_report", SAMPLE_SIZE);
    }

    private void seedEnvironmentalLicense() {
        if (!licenseUseCase.findAll(0, 1).isEmpty()) {
            return;
        }
        String[] facilities = {"Aterro Sanitario", "Usina de Compostagem", "Parque Solar Norte",
                "Estacao de Tratamento", "Terminal Rodoviario"};
        LicenseType[] types = LicenseType.values();
        for (int i = 0; i < SAMPLE_SIZE; i++) {
            LicenseType licenseType = types[i % types.length];
            Instant issueDate = clock.instant().minus(365L + i, ChronoUnit.DAYS);
            Instant expirationDate = clock.instant().plus((i * 15L) - 20, ChronoUnit.DAYS);
            Map<String, Object> requirements = new LinkedHashMap<>();
            if (licenseType == LicenseType.OPERATION) {
                requirements.put("monitoramentoEfluentes", "trimestral");
                requirements.put("relatorioAnualObrigatorio", true);
            } else if (licenseType == LicenseType.INSTALLATION) {
                requirements.put("planoDeContingencia", "obrigatorio");
            } else {
                requirements.put("estudoDeImpactoAmbiental", "em analise");
            }
            EnvironmentalLicense sample = EnvironmentalLicense.builder()
                    .licenseNumber("LIC-2026-%04d".formatted(i + 1))
                    .facility(facilities[i % facilities.length])
                    .licenseType(licenseType)
                    .status(expirationDate.isBefore(clock.instant()) ? LicenseStatus.EXPIRED : LicenseStatus.ACTIVE)
                    .issueDate(issueDate)
                    .expirationDate(expirationDate)
                    .issuingAuthority("CETESB")
                    .additionalRequirements(requirements)
                    .build();
            licenseUseCase.create(sample);
        }
        log.info("Seed: {} documentos inseridos em environmental_license", SAMPLE_SIZE);
    }
}
