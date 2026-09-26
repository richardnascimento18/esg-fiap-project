package com.ecocity.esg.adapter.in.web.config;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public final class EntityTags {
    private EntityTags() { }

    public static String forVersion(Long version) {
        return "\"" + version + "\"";
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
