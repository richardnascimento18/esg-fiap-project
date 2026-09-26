package com.ecocity.esg.domain.model;

import com.ecocity.esg.domain.exception.DomainValidationException;

public record ReportingQuarter(int year, int quarter) {
    public ReportingQuarter {
        if (year < 1 || year > 9999 || quarter < 1 || quarter > 4) {
            throw new DomainValidationException("reportingPeriod deve usar AAAA-Q1 a AAAA-Q4");
        }
    }

    public static ReportingQuarter parse(String value) {
        if (value == null || !value.matches("[0-9]{4}-Q[1-4]")) {
            throw new DomainValidationException("reportingPeriod deve usar AAAA-Q1 a AAAA-Q4");
        }
        return new ReportingQuarter(Integer.parseInt(value.substring(0, 4)),
                Integer.parseInt(value.substring(6)));
    }

    @Override
    public String toString() {
        return "%04d-Q%d".formatted(year, quarter);
    }
}
