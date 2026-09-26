package com.ecocity.esg.adapter.out.persistence.mongodb.initialization;

import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.Indexes;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
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
        for (String collection : new String[] {"energy_consumption", "waste_collection", "carbon_emission",
                "diversity_report", "environmental_license"}) {
            database.getCollection(collection).updateMany(Filters.exists("version", false), Updates.set("version", 0L));
        }
        database.getCollection("environmental_license").createIndex(Indexes.ascending("licenseNumber"), new IndexOptions().unique(true));
        database.getCollection("environmental_license").createIndex(Indexes.ascending("expirationDate"));
    }
}
