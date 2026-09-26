package com.ecocity.esg.domain.model;

import com.ecocity.esg.domain.exception.DomainValidationException;

import java.time.YearMonth;

final class DomainRules {
    private DomainRules() { }

    static void requiredText(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new DomainValidationException(field + " e obrigatorio");
        }
    }

    static void required(Object value, String field) {
        if (value == null) {
            throw new DomainValidationException(field + " e obrigatorio");
        }
    }

    static void nonNegative(double value, String field) {
        if (!Double.isFinite(value) || value < 0) {
            throw new DomainValidationException(field + " deve ser um numero finito nao negativo");
        }
    }

    static void percentage(double value, String field) {
        if (!Double.isFinite(value) || value < 0 || value > 100) {
            throw new DomainValidationException(field + " deve estar entre 0 e 100");
        }
    }

    static void reportingMonth(String value) {
        try {
            YearMonth.parse(value);
        } catch (RuntimeException ex) {
            throw new DomainValidationException("reportingMonth deve usar AAAA-MM");
        }
    }

}
