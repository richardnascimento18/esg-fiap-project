package com.ecocity.esg.application.usecase;

import com.ecocity.esg.application.port.in.IdempotencyUseCase;
import com.ecocity.esg.application.port.out.IdempotencyReservationPort;
import com.ecocity.esg.domain.exception.DomainValidationException;

public class IdempotencyService implements IdempotencyUseCase {
    private final IdempotencyReservationPort reservations;

    public IdempotencyService(IdempotencyReservationPort reservations) {
        this.reservations = reservations;
    }

    @Override
    public String reserve(String resource, String key, String fingerprint) {
        if (key == null) return null;
        if (!key.matches("[A-Za-z0-9._-]{1,128}")) {
            throw new DomainValidationException("Idempotency-Key invalida");
        }
        return reservations.reserve(resource, key, fingerprint);
    }
}
