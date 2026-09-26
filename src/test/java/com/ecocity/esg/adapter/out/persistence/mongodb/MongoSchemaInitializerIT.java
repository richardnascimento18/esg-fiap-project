package com.ecocity.esg.adapter.out.persistence.mongodb;

import com.ecocity.esg.adapter.out.persistence.mongodb.initialization.MongoSchemaInitializer;
import com.ecocity.esg.support.MongoIntegrationTest;
import com.mongodb.MongoWriteException;
import com.mongodb.client.MongoClient;
import org.bson.Document;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

class MongoSchemaInitializerIT extends MongoIntegrationTest {
    @Autowired private MongoClient client;

    @Test
    void createsTheOriginalIndexesAndRecoversAfterDatabaseReset() {
        MongoTemplate template = new MongoTemplate(client, "schema_test_" + UUID.randomUUID().toString().replace("-", ""));
        MongoSchemaInitializer initializer = new MongoSchemaInitializer(template);
        Map<String, Map<String, Integer>> expected = Map.of(
                "environmental_license", Map.of("licenseNumber", 1, "expirationDate", 1));
        try {
            for (int run = 0; run < 2; run++) {
                initializer.run(null);
                var licenses = template.getCollection("environmental_license");
                licenses.insertOne(new Document("licenseNumber", "KEEP"));
                initializer.run(null);
                assertThat(licenses.countDocuments()).isEqualTo(1);
                assertThat(licenses.find().first().getLong("version")).isEqualTo(0L);
                expected.forEach((collection, indexes) -> {
                    var actual = template.getCollection(collection).listIndexes().into(new ArrayList<>());
                    assertThat(actual).hasSize(indexes.size() + 1);
                    indexes.forEach((field, direction) -> assertThat(actual).anySatisfy(index -> {
                        assertThat(index.get("key", Document.class)).isEqualTo(new Document(field, direction));
                        assertThat(index.getString("name")).isEqualTo(field + "_" + direction);
                        if (field.equals("licenseNumber")) assertThat(index.getBoolean("unique")).isTrue();
                    }));
                });
                assertThatThrownBy(() -> licenses.insertOne(new Document("licenseNumber", "KEEP")))
                        .isInstanceOf(MongoWriteException.class);
                template.getDb().drop();
            }
        } finally {
            template.getDb().drop();
        }
    }
}
