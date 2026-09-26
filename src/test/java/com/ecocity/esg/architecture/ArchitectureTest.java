package com.ecocity.esg.architecture;

import com.ecocity.esg.application.port.in.*;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;

@AnalyzeClasses(packages = "com.ecocity.esg", importOptions = ImportOption.DoNotIncludeTests.class)
class ArchitectureTest {

    @Test
    void mutableInputPortsRequireExpectedVersion() {
        for (Class<?> port : List.of(EnergyConsumptionUseCase.class, WasteCollectionUseCase.class,
                CarbonEmissionUseCase.class, DiversityReportUseCase.class, EnvironmentalLicenseUseCase.class)) {
            for (var method : port.getDeclaredMethods()) {
                if (method.getName().equals("update") || method.getName().equals("delete")) {
                    assertThat(method.getParameterTypes()).as(port.getSimpleName() + "." + method.getName())
                            .contains(long.class);
                }
            }
        }
    }

    @ArchTest
    static final ArchRule domainPointsInward = noClasses().that().resideInAPackage("..domain..")
            .should().dependOnClassesThat().resideInAnyPackage("..application..", "..adapter..", "..infrastructure..");

    @ArchTest
    static final ArchRule applicationPointsInward = noClasses().that().resideInAPackage("..application..")
            .should().dependOnClassesThat().resideInAnyPackage("..adapter..", "..infrastructure..");

    @ArchTest
    static final ArchRule coreIsFrameworkIndependent = noClasses()
            .that().resideInAnyPackage("..domain..", "..application..")
            .should().dependOnClassesThat().resideInAnyPackage(
                    "org.springframework..", "com.mongodb..", "org.bson..", "jakarta..", "io.swagger..", "org.slf4j..");

    @ArchTest
    static final ArchRule drivingAdaptersUseInputPorts = noClasses().that().resideInAPackage("..adapter.in..")
            .should().dependOnClassesThat().resideInAnyPackage(
                    "..adapter.out..", "..application.usecase..", "..application.port.out..", "..infrastructure..");

    @ArchTest
    static final ArchRule mongoIsIsolated = noClasses()
            .that().resideOutsideOfPackage("..adapter.out.persistence.mongodb..")
            .should().dependOnClassesThat().resideInAnyPackage(
                    "com.mongodb..", "org.bson..", "org.springframework.data.mongodb..",
                    "..adapter.out.persistence.mongodb..");

    @ArchTest
    static final ArchRule schedulingIsIsolated = noClasses()
            .that().resideOutsideOfPackage("..adapter.in.scheduler..")
            .should().dependOnClassesThat().resideInAPackage("org.springframework.scheduling..");

    @ArchTest
    static final ArchRule layersHaveNoCycles = slices().matching("com.ecocity.esg.(*)..")
            .should().beFreeOfCycles();
}
