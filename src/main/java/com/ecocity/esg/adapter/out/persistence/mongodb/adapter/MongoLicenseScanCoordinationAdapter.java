package com.ecocity.esg.adapter.out.persistence.mongodb.adapter;

import com.ecocity.esg.application.port.out.LicenseScanCoordinationPort;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.FindOneAndUpdateOptions;
import com.mongodb.client.model.ReturnDocument;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

@Component
public class MongoLicenseScanCoordinationAdapter implements LicenseScanCoordinationPort {
    private static final Logger log = LoggerFactory.getLogger(MongoLicenseScanCoordinationAdapter.class);
    private static final String LOCK_ID = "license-renewal-scan";
    private final MongoTemplate template;
    private final Duration lease;

    public MongoLicenseScanCoordinationAdapter(MongoTemplate template,
            @Value("${app.license-alert.lease:PT1M}") Duration lease) {
        if (lease.compareTo(Duration.ofSeconds(15)) < 0 || lease.compareTo(Duration.ofHours(1)) > 0) {
            throw new IllegalArgumentException("License scan lease must be between 15 seconds and 1 hour");
        }
        this.template = template;
        this.lease = lease;
    }

    @Override
    public boolean runIfLeader(Runnable task) {
        MongoCollection<Document> locks = template.getCollection("scheduler_lock");
        String owner = UUID.randomUUID().toString();
        try {
            Document claimed = locks.findOneAndUpdate(Filters.eq("_id", LOCK_ID), acquireUpdate(owner),
                    new FindOneAndUpdateOptions().upsert(true).returnDocument(ReturnDocument.AFTER));
            if (claimed == null || !owner.equals(claimed.getString("owner"))) return false;
        } catch (com.mongodb.MongoWriteException duplicate) {
            if (duplicate.getError().getCode() != 11000) throw duplicate;
            return false;
        }

        Thread runner = Thread.currentThread();
        AtomicBoolean lost = new AtomicBoolean();
        ScheduledExecutorService heartbeat = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread thread = new Thread(r, "license-scan-lease");
            thread.setDaemon(true);
            return thread;
        });
        long interval = Math.max(1, lease.toMillis() / 4);
        heartbeat.scheduleAtFixedRate(() -> {
            try {
                long renewed = locks.updateOne(new Document("_id", LOCK_ID)
                                .append("owner", owner)
                                .append("$expr", new Document("$gt", List.of("$expiresAt", "$$NOW"))),
                        leaseUpdate(owner)).getModifiedCount();
                if (renewed != 1) {
                    lost.set(true);
                    runner.interrupt();
                }
            } catch (RuntimeException ex) {
                log.error("License scan lease renewal failed: type={}", ex.getClass().getSimpleName());
                lost.set(true);
                runner.interrupt();
            }
        }, interval, interval, TimeUnit.MILLISECONDS);
        try {
            task.run();
            if (lost.get()) throw new IllegalStateException("License scan lease was lost");
            return true;
        } finally {
            heartbeat.shutdownNow();
            locks.deleteOne(Filters.and(Filters.eq("_id", LOCK_ID), Filters.eq("owner", owner)));
            if (lost.get()) Thread.interrupted();
        }
    }

    private List<Document> leaseUpdate(String owner) {
        return List.of(new Document("$set", new Document("owner", owner)
                .append("expiresAt", expirationExpression())));
    }

    private List<Document> acquireUpdate(String owner) {
        Document expired = new Document("$lte", List.of(
                new Document("$ifNull", List.of("$expiresAt", new Date(0))), "$$NOW"));
        return List.of(new Document("$set", new Document("owner",
                new Document("$cond", List.of(expired, owner, "$owner")))
                .append("expiresAt", new Document("$cond", List.of(expired,
                        expirationExpression(), "$expiresAt")))));
    }

    private Document expirationExpression() {
        return new Document("$dateAdd", new Document("startDate", "$$NOW")
                .append("unit", "millisecond").append("amount", lease.toMillis()));
    }
}
