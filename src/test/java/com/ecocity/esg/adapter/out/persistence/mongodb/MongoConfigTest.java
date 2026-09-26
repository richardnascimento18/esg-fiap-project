package com.ecocity.esg.adapter.out.persistence.mongodb;

import com.ecocity.esg.adapter.out.persistence.mongodb.config.MongoConfig;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MongoConfigTest {
    @Test
    void rejectsUnboundedDependencyTimeouts() {
        MongoConfig config = new MongoConfig();
        assertThatThrownBy(() -> config.mongoClientSettingsBuilderCustomizer(
                Duration.ZERO, Duration.ofSeconds(15), Duration.ofSeconds(5)))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> config.mongoClientSettingsBuilderCustomizer(
                Duration.ofSeconds(5), Duration.ofMinutes(2), Duration.ofSeconds(5)))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
