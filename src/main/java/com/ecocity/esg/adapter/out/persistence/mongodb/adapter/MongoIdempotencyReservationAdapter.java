package com.ecocity.esg.adapter.out.persistence.mongodb.adapter;

import com.ecocity.esg.application.port.out.IdempotencyReservationPort;
import com.ecocity.esg.domain.exception.IdempotencyConflictException;
import com.mongodb.client.MongoCollection;
import org.bson.Document;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;
import com.mongodb.client.model.Updates;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@Component
public class MongoIdempotencyReservationAdapter implements IdempotencyReservationPort {
    private final MongoTemplate template;

    public MongoIdempotencyReservationAdapter(MongoTemplate template) {
        this.template = template;
    }

    @Override
    public String reserve(String resource, String key, String fingerprint) {
        String digest = sha256(resource + ":" + key);
        String id = "idem-" + digest;
        MongoCollection<Document> collection = template.getCollection("idempotency_reservation");
        try {
            collection.insertOne(new Document("_id", id).append("fingerprint", fingerprint));
        } catch (com.mongodb.MongoWriteException ex) {
            if (ex.getError().getCode() != 11000) throw ex;
            Document previous = collection.find(new Document("_id", id)).first();
            if (previous == null || Boolean.TRUE.equals(previous.getBoolean("deleted"))
                    || !fingerprint.equals(previous.getString("fingerprint"))) {
                throw new IdempotencyConflictException();
            }
        }
        return id;
    }

    @Override
    public void markDeleted(String resourceId) {
        if (resourceId != null && resourceId.startsWith("idem-")) {
            template.getCollection("idempotency_reservation")
                    .updateOne(new Document("_id", resourceId), Updates.set("deleted", true));
        }
    }

    private static String sha256(String value) {
        try {
            byte[] hash = MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
            return java.util.HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 is unavailable", ex);
        }
    }
}
