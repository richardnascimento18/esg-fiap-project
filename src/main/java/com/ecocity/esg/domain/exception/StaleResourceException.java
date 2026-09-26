package com.ecocity.esg.domain.exception;

public class StaleResourceException extends RuntimeException {
    public StaleResourceException() {
        super("Resource version is stale");
    }
}
