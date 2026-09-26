package com.ecocity.esg.adapter.in.web;

import com.ecocity.esg.support.MongoIntegrationTest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

class IdempotentCreateIT extends MongoIntegrationTest {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper mapper;

    @Test
    void concurrentRetriesCreateOneLogicalResource() throws Exception {
        String key = UUID.randomUUID().toString();
        String body = """
                {"district":"Centro","wasteType":"RECYCLABLE","weightKg":10,
                 "recyclingRatePercentage":50,"collectionDate":"2026-01-01T00:00:00Z",
                 "collectorTeam":"Equipe","properlyDisposed":true}
                """;
        CountDownLatch start = new CountDownLatch(1);
        try (var executor = Executors.newFixedThreadPool(2)) {
            var call = (java.util.concurrent.Callable<String>) () -> {
                start.await();
                var response = mvc.perform(post("/api/v1/waste-collections")
                        .with(httpBasic("test-editor", "test-editor-password"))
                        .header("Idempotency-Key", key).contentType("application/json").content(body))
                        .andReturn().getResponse();
                assertThat(response.getStatus()).isEqualTo(201);
                return mapper.readTree(response.getContentAsString()).path("id").asText();
            };
            var first = executor.submit(call);
            var second = executor.submit(call);
            start.countDown();
            assertThat(first.get()).isEqualTo(second.get());
        }
    }
}
