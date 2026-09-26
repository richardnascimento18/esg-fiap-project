package com.ecocity.esg.adapter.in.web.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RequestFingerprintTest {
    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    void nestedObjectOrderDoesNotChangeFingerprint() throws Exception {
        String first = """
                {"metadata":{"sensor":"A","nested":{"region":"north","floor":2}},"count":1}
                """;
        String reordered = """
                {"count":1,"metadata":{"nested":{"floor":2,"region":"north"},"sensor":"A"}}
                """;
        assertThat(fingerprint(first)).isEqualTo(fingerprint(reordered));
    }

    @Test
    void arrayOrderAndValueTypesRemainSignificant() throws Exception {
        assertThat(fingerprint("{\"values\":[1,2]}")).isNotEqualTo(fingerprint("{\"values\":[2,1]}"));
        assertThat(fingerprint("{\"value\":1}")).isNotEqualTo(fingerprint("{\"value\":\"1\"}"));
        assertThat(fingerprint("{\"value\":1}")).isNotEqualTo(fingerprint("{\"value\":2}"));
    }

    private String fingerprint(String json) throws Exception {
        return RequestFingerprint.of(mapper.readTree(json), mapper);
    }
}
