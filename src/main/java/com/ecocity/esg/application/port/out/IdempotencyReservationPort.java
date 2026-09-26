package com.ecocity.esg.application.port.out;

public interface IdempotencyReservationPort {
    String reserve(String resource, String key, String payloadFingerprint);

    void markDeleted(String resourceId);
}
