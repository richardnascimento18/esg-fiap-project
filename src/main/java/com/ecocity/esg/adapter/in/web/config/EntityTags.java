package com.ecocity.esg.adapter.in.web.config;

import com.ecocity.esg.domain.model.EnvironmentalLicense;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public final class EntityTags {
    private EntityTags() { }

    public static String forVersion(Long version) {
        return "\"" + version + "\"";
    }

    public static String forLicense(EnvironmentalLicense license) {
        return "\"" + license.getVersion() + "-" + license.getStatus().name() + "\"";
    }

    public static long requiredLicenseVersion(String ifMatch) {
        if (ifMatch == null) {
            throw new ResponseStatusException(HttpStatus.PRECONDITION_REQUIRED, "If-Match is required");
        }
        if (!ifMatch.matches("\"(0|[1-9][0-9]*)-(ACTIVE|EXPIRED|SUSPENDED|RENEWAL_IN_PROGRESS)\"")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid If-Match");
        }
        try {
            return Long.parseLong(ifMatch.substring(1, ifMatch.indexOf('-')));
        } catch (NumberFormatException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid If-Match");
        }
    }

    public static void requireLicenseMatch(String ifMatch, EnvironmentalLicense current) {
        if (!ifMatch.equals(forLicense(current))) {
            throw new ResponseStatusException(HttpStatus.PRECONDITION_FAILED, "Resource has changed");
        }
    }

    public static long requiredVersion(String ifMatch) {
        if (ifMatch == null) {
            throw new ResponseStatusException(HttpStatus.PRECONDITION_REQUIRED, "If-Match is required");
        }
        if (!ifMatch.matches("\"(0|[1-9][0-9]*)\"")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid If-Match");
        }
        try {
            return Long.parseLong(ifMatch.substring(1, ifMatch.length() - 1));
        } catch (NumberFormatException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid If-Match");
        }
    }
}
