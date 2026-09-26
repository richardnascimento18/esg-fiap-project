package com.ecocity.esg.adapter.out.persistence.mongodb;

import com.ecocity.esg.adapter.out.persistence.mongodb.adapter.MongoLicenseScanCoordinationAdapter;
import com.ecocity.esg.application.port.out.LicenseScanCoordinationPort;
import com.ecocity.esg.support.MongoIntegrationTest;
import org.bson.Document;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.util.Date;
import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.*;

class MongoLicenseScanCoordinationIT extends MongoIntegrationTest {
    @Autowired LicenseScanCoordinationPort coordination;
    @Autowired MongoTemplate template;

    @Test
    void simultaneousInstancesYieldOneOwner() throws Exception {
        template.getCollection("scheduler_lock").deleteOne(new Document("_id", "license-renewal-scan"));
        var firstInstance = new MongoLicenseScanCoordinationAdapter(template, Duration.ofSeconds(15));
        var secondInstance = new MongoLicenseScanCoordinationAdapter(template, Duration.ofSeconds(15));
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        try (var executor = Executors.newFixedThreadPool(2)) {
            try {
                var completed = new ExecutorCompletionService<Boolean>(executor);
                completed.submit(() -> {
                    start.await();
                    return firstInstance.runIfLeader(() -> await(release));
                });
                completed.submit(() -> {
                    start.await();
                    return secondInstance.runIfLeader(() -> await(release));
                });
                start.countDown();
                assertThat(completed.poll(5, TimeUnit.SECONDS).get()).isFalse();
                release.countDown();
                assertThat(completed.poll(5, TimeUnit.SECONDS).get()).isTrue();
            } finally {
                start.countDown();
                release.countDown();
            }
        }
    }

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

    @Test
    void oldOwnerCannotReleaseLeaseAfterAnotherInstanceTakesOver() throws Exception {
        var locks = template.getCollection("scheduler_lock");
        locks.deleteOne(new Document("_id", "license-renewal-scan"));
        var firstInstance = new MongoLicenseScanCoordinationAdapter(template, Duration.ofSeconds(15));
        var secondInstance = new MongoLicenseScanCoordinationAdapter(template, Duration.ofSeconds(15));
        CountDownLatch firstStarted = new CountDownLatch(1);
        CountDownLatch secondStarted = new CountDownLatch(1);
        CountDownLatch releaseFirst = new CountDownLatch(1);
        CountDownLatch releaseSecond = new CountDownLatch(1);
        try (var executor = Executors.newFixedThreadPool(2)) {
            try {
                var first = executor.submit(() -> firstInstance.runIfLeader(() -> {
                    firstStarted.countDown();
                    await(releaseFirst);
                }));
                assertThat(firstStarted.await(5, TimeUnit.SECONDS)).isTrue();
                assertThat(secondInstance.runIfLeader(() -> fail("Active lease was acquired"))).isFalse();

                // Simulate a crashed/stalled first owner whose lease has expired at Mongo.
                locks.updateOne(new Document("_id", "license-renewal-scan"),
                        new Document("$set", new Document("expiresAt", new Date(0))));
                var second = executor.submit(() -> secondInstance.runIfLeader(() -> {
                    secondStarted.countDown();
                    await(releaseSecond);
                }));
                assertThat(secondStarted.await(5, TimeUnit.SECONDS)).isTrue();
                String newOwner = locks.find(new Document("_id", "license-renewal-scan"))
                        .first().getString("owner");

                releaseFirst.countDown();
                assertThat(first.get(5, TimeUnit.SECONDS)).isTrue();
                assertThat(locks.find(new Document("_id", "license-renewal-scan"))
                        .first().getString("owner")).isEqualTo(newOwner);
                releaseSecond.countDown();
                assertThat(second.get(5, TimeUnit.SECONDS)).isTrue();
                assertThat(locks.countDocuments(new Document("_id", "license-renewal-scan"))).isZero();
            } finally {
                releaseFirst.countDown();
                releaseSecond.countDown();
            }
        }
    }

    @Test
    void activeOwnerRenewsUsingMongoTime() throws Exception {
        var locks = template.getCollection("scheduler_lock");
        locks.deleteOne(new Document("_id", "license-renewal-scan"));
        var instance = new MongoLicenseScanCoordinationAdapter(template, Duration.ofSeconds(15));
        CountDownLatch started = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        try (var executor = Executors.newSingleThreadExecutor()) {
            try {
                var run = executor.submit(() -> instance.runIfLeader(() -> {
                    started.countDown();
                    await(release);
                }));
                assertThat(started.await(5, TimeUnit.SECONDS)).isTrue();
                Date before = locks.find(new Document("_id", "license-renewal-scan"))
                        .first().getDate("expiresAt");
                Thread.sleep(5_000);
                Date after = locks.find(new Document("_id", "license-renewal-scan"))
                        .first().getDate("expiresAt");
                assertThat(after).isAfter(before);
                release.countDown();
                assertThat(run.get(5, TimeUnit.SECONDS)).isTrue();
            } finally {
                release.countDown();
            }
        }
    }

    private static void await(CountDownLatch latch) {
        try {
            if (!latch.await(10, TimeUnit.SECONDS)) throw new IllegalStateException("Timed out waiting for test release");
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Test task interrupted", ex);
        }
    }
}
