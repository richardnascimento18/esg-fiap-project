package com.ecocity.esg.adapter.out.persistence.mongodb.adapter;

import com.ecocity.esg.application.port.out.LicenseScanCoordinationPort;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Clock;
import java.time.Duration;
import java.util.Date;
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
    private final Clock clock;
    private final Duration lease;

    public MongoLicenseScanCoordinationAdapter(MongoTemplate template, Clock clock,
            @Value("${app.license-alert.lease:PT1M}") Duration lease) {
        if (lease.compareTo(Duration.ofSeconds(15)) < 0 || lease.compareTo(Duration.ofHours(1)) > 0) {
            throw new IllegalArgumentException("License scan lease must be between 15 seconds and 1 hour");
        }
        this.template = template;
        this.clock = clock;
        this.lease = lease;
    }

    @Override
    public boolean runIfLeader(Runnable task) {
        MongoCollection<Document> locks = template.getCollection("scheduler_lock");
        String owner = UUID.randomUUID().toString();
        Date now = Date.from(clock.instant());
        Date expiration = Date.from(clock.instant().plus(lease));
        try {
            locks.insertOne(new Document("_id", LOCK_ID).append("owner", owner).append("expiresAt", expiration));
        } catch (com.mongodb.MongoWriteException duplicate) {
            if (duplicate.getError().getCode() != 11000) throw duplicate;
            Document claimed = locks.findOneAndUpdate(
                    Filters.and(Filters.eq("_id", LOCK_ID), Filters.lt("expiresAt", now)),
                    Updates.combine(Updates.set("owner", owner), Updates.set("expiresAt", expiration)));
            if (claimed == null) return false;
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
                long renewed = locks.updateOne(Filters.and(Filters.eq("_id", LOCK_ID),
                                Filters.eq("owner", owner), Filters.gt("expiresAt", Date.from(clock.instant()))),
                        Updates.set("expiresAt", Date.from(clock.instant().plus(lease)))).getModifiedCount();
                if (renewed != 1) {
                    lost.set(true);
                    runner.interrupt();
                }
            } catch (RuntimeException ex) {
                log.error("License scan lease renewal failed", ex);
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
}
