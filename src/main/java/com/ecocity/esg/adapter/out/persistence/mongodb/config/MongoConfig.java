package com.ecocity.esg.adapter.out.persistence.mongodb.config;

import org.springframework.boot.autoconfigure.mongo.MongoClientSettingsBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Value;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

/** Connection pool settings for the MongoDB persistence adapter. */
@Configuration
public class MongoConfig {

    @Bean
    public MongoClientSettingsBuilderCustomizer mongoClientSettingsBuilderCustomizer(
            @Value("${app.mongo.connect-timeout}") Duration connectTimeout,
            @Value("${app.mongo.read-timeout}") Duration readTimeout,
            @Value("${app.mongo.server-selection-timeout}") Duration serverSelectionTimeout) {
        validate(connectTimeout, "connect-timeout");
        validate(readTimeout, "read-timeout");
        validate(serverSelectionTimeout, "server-selection-timeout");
        return builder -> {
            builder.applyToConnectionPoolSettings(poolBuilder -> poolBuilder
                    .minSize(0)
                    .maxSize(50)
                    .maxWaitTime(5, TimeUnit.SECONDS)
                    .maxConnectionIdleTime(60, TimeUnit.SECONDS));
            builder.applyToSocketSettings(socket -> socket
                    .connectTimeout(connectTimeout.toMillis(), TimeUnit.MILLISECONDS)
                    .readTimeout(readTimeout.toMillis(), TimeUnit.MILLISECONDS));
            builder.applyToClusterSettings(cluster -> cluster
                    .serverSelectionTimeout(serverSelectionTimeout.toMillis(), TimeUnit.MILLISECONDS));
        };
    }

    private void validate(Duration duration, String name) {
        if (duration.compareTo(Duration.ofSeconds(1)) < 0 || duration.compareTo(Duration.ofMinutes(1)) > 0) {
            throw new IllegalArgumentException("Mongo " + name + " must be between 1 and 60 seconds");
        }
    }
}
