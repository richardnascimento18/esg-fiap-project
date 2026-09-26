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
                {"facilityId":"F1","facilityName":"Facility","city":"City","sourceType":"SOLAR",
                 "consumptionKwh":10,"thresholdKwh":20,"readingTimestamp":"2026-01-01T00:00:00Z",
                 "sensorMetadata":{"sensor":"A","nested":{"region":"north","floor":2}}}
                """;
        String reordered = body.replace("\"sensor\":\"A\",\"nested\":{\"region\":\"north\",\"floor\":2}",
                "\"nested\":{\"floor\":2,\"region\":\"north\"},\"sensor\":\"A\"");
        CountDownLatch start = new CountDownLatch(1);
        try (var executor = Executors.newFixedThreadPool(2)) {
            java.util.function.Function<String, java.util.concurrent.Callable<String>> call = payload -> () -> {
                start.await();
                var response = mvc.perform(post("/api/v1/energy-consumptions")
                        .with(httpBasic("test-editor", "test-editor-password"))
                        .header("Idempotency-Key", key).contentType("application/json").content(payload))
                        .andReturn().getResponse();
                assertThat(response.getStatus()).isEqualTo(201);
                return mapper.readTree(response.getContentAsString()).path("id").asText();
            };
            var first = executor.submit(call.apply(body));
            var second = executor.submit(call.apply(reordered));
            start.countDown();
            assertThat(first.get()).isEqualTo(second.get());
        }
    }
}
