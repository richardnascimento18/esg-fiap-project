package com.ecocity.esg.adapter.in.web;

import com.ecocity.esg.support.WebContractTest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebContractTest
class IdempotentCreateTest {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper mapper;
    private static final String URL = "/api/v1/energy-consumptions";
    private static final String BODY = """
            {"facilityId":"F1","facilityName":"Facility","city":"City","sourceType":"SOLAR",
             "consumptionKwh":10,"thresholdKwh":20,"readingTimestamp":"2026-01-01T00:00:00Z"}
            """;

    @Test
    void retryReturnsSameResourceAndDifferentPayloadConflicts() throws Exception {
        String key = java.util.UUID.randomUUID().toString();
        var first = mvc.perform(post(URL).header("Idempotency-Key", key)
                .contentType("application/json").content(BODY)).andExpect(status().isCreated())
                .andReturn().getResponse();
        var retry = mvc.perform(post(URL).header("Idempotency-Key", key)
                .contentType("application/json").content(BODY)).andExpect(status().isCreated())
                .andReturn().getResponse();
        assertThat(mapper.readTree(retry.getContentAsString()).path("id"))
                .isEqualTo(mapper.readTree(first.getContentAsString()).path("id"));
        mvc.perform(post(URL).header("Idempotency-Key", key).contentType("application/json")
                .content(BODY.replace("\"consumptionKwh\":10", "\"consumptionKwh\":11")))
                .andExpect(status().isConflict());
    }
}
