package com.ecocity.esg.application.port.in;

public interface IdempotencyUseCase {
    String reserve(String resource, String key, String payloadFingerprint);
}
