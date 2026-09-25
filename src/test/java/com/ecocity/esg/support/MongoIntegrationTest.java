package com.ecocity.esg.support;

import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@org.springframework.security.test.context.support.WithMockUser(roles = "ADMIN")
public abstract class MongoIntegrationTest {

    // JVM-scoped lifecycle keeps cached Spring contexts connected to the same container.
    // Testcontainers removes the container when the test JVM exits.
    private static final String MONGO_URI = testMongoUri();

    private static String testMongoUri() {
        String externalUri = System.getProperty("test.mongodb.uri");
        if (externalUri != null && !externalUri.isBlank()) {
            return externalUri;
        }
        MongoDBContainer container = new MongoDBContainer("mongo:7.0");
        container.start();
        return container.getReplicaSetUrl();
    }

    @DynamicPropertySource
    public static void mongoProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", () -> MONGO_URI);
    }
}
