package com.ecocity.esg.adapter.out.persistence.mongodb;

import com.ecocity.esg.application.port.out.LicenseScanCoordinationPort;
import com.ecocity.esg.support.MongoIntegrationTest;
import org.bson.Document;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.*;

class MongoLicenseScanCoordinationIT extends MongoIntegrationTest {
    @Autowired LicenseScanCoordinationPort coordination;
    @Autowired MongoTemplate template;

    @Test
    void excludesConcurrentRunnerAndReleasesAfterCompletion() {
        AtomicInteger runs = new AtomicInteger();
        assertThat(coordination.runIfLeader(() -> {
            runs.incrementAndGet();
            assertThat(coordination.runIfLeader(runs::incrementAndGet)).isFalse();
        })).isTrue();
        assertThat(coordination.runIfLeader(runs::incrementAndGet)).isTrue();
        assertThat(runs).hasValue(2);
    }

    @Test
    void expiredLeaseCanBeClaimedAfterCrash() {
        var locks = template.getCollection("scheduler_lock");
        locks.deleteOne(new Document("_id", "license-renewal-scan"));
        locks.insertOne(new Document("_id", "license-renewal-scan")
                .append("owner", "crashed-instance").append("expiresAt", new Date(0)));
        assertThat(coordination.runIfLeader(() -> {})).isTrue();
        assertThat(locks.countDocuments(new Document("_id", "license-renewal-scan"))).isZero();
    }
}
