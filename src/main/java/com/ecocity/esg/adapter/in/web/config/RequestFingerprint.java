package com.ecocity.esg.adapter.in.web.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public final class RequestFingerprint {
    private RequestFingerprint() { }

    public static String of(Object request, ObjectMapper mapper) {
        try {
            return java.util.HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                    .digest(mapper.writeValueAsBytes(canonical(mapper.valueToTree(request), mapper))));
        } catch (JsonProcessingException | NoSuchAlgorithmException ex) {
            throw new IllegalStateException("Cannot fingerprint request", ex);
        }
    }

    private static JsonNode canonical(JsonNode node, ObjectMapper mapper) {
        if (node.isObject()) {
            ObjectNode sorted = mapper.createObjectNode();
            node.properties().stream().sorted(java.util.Map.Entry.comparingByKey())
                    .forEach(entry -> sorted.set(entry.getKey(), canonical(entry.getValue(), mapper)));
            return sorted;
        }
        if (node.isArray()) {
            ArrayNode ordered = mapper.createArrayNode();
            node.forEach(value -> ordered.add(canonical(value, mapper)));
            return ordered;
        }
        return node;
    }
}
