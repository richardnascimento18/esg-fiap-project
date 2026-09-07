package com.ecocity.esg.adapter.out.persistence.mongodb.initialization;

import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.Indexes;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

/**
 * Reconciles the required indexes on every startup, before development seeding.
 * MongoDB creates missing collections with the indexes and retains existing data.
 * Conflicting indexes fail startup instead of being silently replaced.
 * This is schema initialization, not a history of data migrations.
 */
@Component
@Order(1)
public class MongoSchemaInitializer implements ApplicationRunner {

    private final MongoTemplate mongoTemplate;

    public MongoSchemaInitializer(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public void run(ApplicationArguments args) {
        MongoDatabase database = mongoTemplate.getDb();
        database.getCollection("energy_consumption").createIndex(Indexes.ascending("facilityId"));
        database.getCollection("energy_consumption").createIndex(Indexes.descending("readingTimestamp"));
        database.getCollection("energy_consumption").createIndex(Indexes.ascending("alertTriggered"));
        database.getCollection("waste_collection").createIndex(Indexes.ascending("district"));
        database.getCollection("waste_collection").createIndex(Indexes.ascending("wasteType"));
        database.getCollection("waste_collection").createIndex(Indexes.descending("collectionDate"));
        database.getCollection("carbon_emission").createIndex(Indexes.ascending("sourceFacility"));
        database.getCollection("carbon_emission").createIndex(Indexes.ascending("reportingPeriod"));
        database.getCollection("carbon_emission").createIndex(Indexes.ascending("compensated"));
        database.getCollection("diversity_report").createIndex(Indexes.ascending("department"));
        database.getCollection("diversity_report").createIndex(Indexes.ascending("reportingMonth"));
        database.getCollection("environmental_license").createIndex(Indexes.ascending("licenseNumber"), new IndexOptions().unique(true));
        database.getCollection("environmental_license").createIndex(Indexes.ascending("status"));
        database.getCollection("environmental_license").createIndex(Indexes.ascending("expirationDate"));
    }
}
