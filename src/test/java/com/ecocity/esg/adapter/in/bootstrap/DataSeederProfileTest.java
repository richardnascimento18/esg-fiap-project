package com.ecocity.esg.adapter.in.bootstrap;

import com.ecocity.esg.application.port.in.CarbonEmissionUseCase;
import com.ecocity.esg.application.port.in.DiversityReportUseCase;
import com.ecocity.esg.application.port.in.EnergyConsumptionUseCase;
import com.ecocity.esg.application.port.in.EnvironmentalLicenseUseCase;
import com.ecocity.esg.application.port.in.WasteCollectionUseCase;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import java.time.Clock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class DataSeederProfileTest {
    private final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withUserConfiguration(DataSeeder.class)
            .withBean(EnergyConsumptionUseCase.class, () -> mock(EnergyConsumptionUseCase.class))
            .withBean(WasteCollectionUseCase.class, () -> mock(WasteCollectionUseCase.class))
            .withBean(CarbonEmissionUseCase.class, () -> mock(CarbonEmissionUseCase.class))
            .withBean(DiversityReportUseCase.class, () -> mock(DiversityReportUseCase.class))
            .withBean(EnvironmentalLicenseUseCase.class, () -> mock(EnvironmentalLicenseUseCase.class))
            .withBean(Clock.class, Clock::systemUTC);

    @Test
    void seederRequiresExplicitDevelopmentProfileAndOptIn() {
        runner.withPropertyValues("app.seed-demo-data=true")
                .run(context -> assertThat(context).doesNotHaveBean(DataSeeder.class));
        runner.withInitializer(context -> context.getEnvironment().setActiveProfiles("dev"))
                .run(context -> assertThat(context).doesNotHaveBean(DataSeeder.class));
        runner.withInitializer(context -> context.getEnvironment().setActiveProfiles("dev", "production"))
                .withPropertyValues("app.seed-demo-data=true")
                .run(context -> assertThat(context).doesNotHaveBean(DataSeeder.class));
        runner.withInitializer(context -> context.getEnvironment().setActiveProfiles("dev"))
                .withPropertyValues("app.seed-demo-data=true")
                .run(context -> assertThat(context).hasSingleBean(DataSeeder.class));
    }
}
