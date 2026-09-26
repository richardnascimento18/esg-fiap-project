package com.ecocity.esg.adapter.in.web;

import com.ecocity.esg.support.WebContractTest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Clock;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebContractTest
class EnvironmentalLicenseEtagTest {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper mapper;
    @MockitoBean Clock clock;

    @Test
    void clockOnlyStatusChangeInvalidatesStrongTagAndConditionalWrite() throws Exception {
        AtomicReference<Instant> now = new AtomicReference<>(Instant.parse("2026-01-01T10:59:00Z"));
        when(clock.instant()).thenAnswer(call -> now.get());
        String body = """
                {"licenseNumber":"LIC-CLOCK","facility":"Parque","licenseType":"OPERATION",
                 "issueDate":"2025-01-01T00:00:00Z","expirationDate":"2026-01-01T11:00:00Z",
                 "issuingAuthority":"CETESB"}
                """;
        var created = mvc.perform(post("/api/v1/environmental-licenses").contentType("application/json").content(body))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.status").value("ACTIVE"))
                .andReturn().getResponse();
        String id = mapper.readTree(created.getContentAsString()).path("id").asText();
        assertThat(created.getHeader("ETag")).isEqualTo("\"0-ACTIVE\"");

        now.set(Instant.parse("2026-01-01T11:01:00Z"));
        mvc.perform(get("/api/v1/environmental-licenses/{id}", id))
                .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("EXPIRED"))
                .andExpect(header().string("ETag", "\"0-EXPIRED\""));
        mvc.perform(put("/api/v1/environmental-licenses/{id}", id).header("If-Match", "\"0-ACTIVE\"")
                        .contentType("application/json").content(body))
                .andExpect(status().isPreconditionFailed());
        mvc.perform(put("/api/v1/environmental-licenses/{id}", id).header("If-Match", "\"0-EXPIRED\"")
                        .contentType("application/json").content(body))
                .andExpect(status().isOk()).andExpect(header().string("ETag", "\"1-EXPIRED\""));
    }
}
