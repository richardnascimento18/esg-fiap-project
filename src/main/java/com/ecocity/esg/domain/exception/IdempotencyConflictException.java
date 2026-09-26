package com.ecocity.esg.domain.exception;

public class IdempotencyConflictException extends RuntimeException {
    public IdempotencyConflictException() {
        super("Idempotency key already used with another payload");
    }
}
