package com.ecocity.esg.adapter.in.web;

import com.ecocity.esg.adapter.in.web.dto.request.EnvironmentalLicenseRequest;
import com.ecocity.esg.domain.model.LicenseType;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import com.ecocity.esg.support.MongoIntegrationTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.UUID;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class EnvironmentalLicenseControllerIT extends MongoIntegrationTest {

    private static final String BASE_URL = "/api/v1/environmental-licenses";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldCreateActiveLicenseWithFlexibleRequirements() throws Exception {
        EnvironmentalLicenseRequest request = EnvironmentalLicenseRequest.builder()
                .licenseNumber("LIC-IT-0001")
                .facility("Aterro Sanitario")
                .licenseType(LicenseType.OPERATION)
                .issueDate(Instant.now().minus(30, ChronoUnit.DAYS))
                .expirationDate(Instant.now().plus(90, ChronoUnit.DAYS))
                .issuingAuthority("CETESB")
                .additionalRequirements(Map.of("monitoramentoEfluentes", "trimestral"))
                .build();

        mockMvc.perform(post(BASE_URL)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status", is("ACTIVE")));
    }

    @Test
    void shouldRejectExpirationDateBeforeIssueDate() throws Exception {
        EnvironmentalLicenseRequest request = EnvironmentalLicenseRequest.builder()
                .licenseNumber("LIC-IT-0002")
                .facility("Aterro Sanitario")
                .licenseType(LicenseType.PRIOR)
                .issueDate(Instant.now())
                .expirationDate(Instant.now().minus(1, ChronoUnit.DAYS))
                .issuingAuthority("CETESB")
                .build();

        mockMvc.perform(post(BASE_URL)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void duplicateLicenseNumberReturnsConflictWithoutDatabaseDetails() throws Exception {
        EnvironmentalLicenseRequest request = EnvironmentalLicenseRequest.builder()
                .licenseNumber("LIC-DUP-" + UUID.randomUUID())
                .facility("Aterro Sanitario")
                .licenseType(LicenseType.OPERATION)
                .issueDate(Instant.now().minus(1, ChronoUnit.DAYS))
                .expirationDate(Instant.now().plus(30, ChronoUnit.DAYS))
                .issuingAuthority("CETESB")
                .build();
        String body = objectMapper.writeValueAsString(request);
        mockMvc.perform(post(BASE_URL).contentType("application/json").content(body))
                .andExpect(status().isCreated());
        mockMvc.perform(post(BASE_URL).contentType("application/json").content(body))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message", is("Registro ja existente")));
    }
}
