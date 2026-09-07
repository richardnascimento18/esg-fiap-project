package com.ecocity.esg.adapter.out.persistence.mongodb.config;

import org.springframework.boot.autoconfigure.mongo.MongoClientSettingsBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

/** Connection pool settings for the MongoDB persistence adapter. */
@Configuration
public class MongoConfig {

    @Bean
    public MongoClientSettingsBuilderCustomizer mongoClientSettingsBuilderCustomizer() {
        return builder -> builder.applyToConnectionPoolSettings(poolBuilder -> poolBuilder
                .minSize(5)
                .maxSize(50)
                .maxWaitTime(5, TimeUnit.SECONDS)
                .maxConnectionIdleTime(60, TimeUnit.SECONDS));
    }
}
