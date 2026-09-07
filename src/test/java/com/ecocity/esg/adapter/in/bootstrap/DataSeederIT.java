package com.ecocity.esg.adapter.in.bootstrap;

import com.ecocity.esg.application.port.in.*;
import com.ecocity.esg.support.MongoIntegrationTest;
import org.bson.Document;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DataSeederIT extends MongoIntegrationTest {
    @Autowired private EnergyConsumptionUseCase energy;
    @Autowired private WasteCollectionUseCase waste;
    @Autowired private CarbonEmissionUseCase carbon;
    @Autowired private DiversityReportUseCase diversity;
    @Autowired private EnvironmentalLicenseUseCase licenses;
    @Autowired private MongoTemplate mongo;

    private static final List<String> COLLECTIONS = List.of("energy_consumption", "waste_collection",
            "carbon_emission", "diversity_report", "environmental_license");

    @BeforeEach
    @AfterEach
    void cleanTestData() {
        COLLECTIONS.forEach(name -> mongo.getCollection(name).deleteMany(new Document()));
    }

    @Test
    void seedsTwelveRecordsPerEmptyCollectionAndSkipsExistingData() {
        Clock clock = Clock.fixed(Instant.parse("2026-09-07T12:00:00Z"), ZoneOffset.UTC);
        DataSeeder seeder = new DataSeeder(energy, waste, carbon, diversity, licenses, clock);
        seeder.run(null);
        var originalIds = energy.findAll(0, 20).stream().map(item -> item.getId()).toList();
        seeder.run(null);
        COLLECTIONS.forEach(name -> assertThat(mongo.getCollection(name).countDocuments()).isEqualTo(12));
        assertThat(energy.findAll(0, 20).stream().map(item -> item.getId()).toList()).containsExactlyElementsOf(originalIds);
        assertThat(carbon.findAll(0, 20)).allSatisfy(item ->
                assertThat(item.isCompensated()).isEqualTo(item.getCompensationTonnes() >= item.getEmissionTonnes()));
        // An individually emptied collection is replenished without changing the others.
        mongo.getCollection("waste_collection").deleteMany(new Document());
        seeder.run(null);
        assertThat(waste.findAll(0, 20)).hasSize(12);
        assertThat(energy.findAll(0, 20).stream().map(item -> item.getId()).toList()).containsExactlyElementsOf(originalIds);
    }
}
