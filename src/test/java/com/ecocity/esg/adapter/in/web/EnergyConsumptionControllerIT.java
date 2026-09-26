package com.ecocity.esg.adapter.in.web;

import com.ecocity.esg.adapter.in.web.dto.request.EnergyConsumptionRequest;
import com.ecocity.esg.domain.model.EnergySourceType;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import com.ecocity.esg.support.MongoIntegrationTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Full-stack integration test: HTTP layer -> use case -> MongoDB adapter -> the
 * MongoDB 7 instance provided by Testcontainers.
 */
class EnergyConsumptionControllerIT extends MongoIntegrationTest {

    private static final String BASE_URL = "/api/v1/energy-consumptions";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private EnergyConsumptionRequest buildRequest(double consumption, double threshold) {
        return EnergyConsumptionRequest.builder()
                .facilityId("FAC-IT-001")
                .facilityName("Paco Municipal")
                .city("Sao Paulo")
                .sourceType(EnergySourceType.GRID)
                .consumptionKwh(consumption)
                .thresholdKwh(threshold)
                .readingTimestamp(Instant.now())
                .build();
    }

    @Test
    void shouldSupportFullCrudLifecycle() throws Exception {
        EnergyConsumptionRequest createRequest = buildRequest(4000, 3000);

        String responseBody = mockMvc.perform(post(BASE_URL)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.alertTriggered", is(true)))
                .andReturn().getResponse().getContentAsString();

        String id = objectMapper.readTree(responseBody).get("id").asText();

        mockMvc.perform(get(BASE_URL + "/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.facilityId", is("FAC-IT-001")));

        EnergyConsumptionRequest updateRequest = buildRequest(1000, 3000);
        mockMvc.perform(put(BASE_URL + "/{id}", id).header("If-Match", "\"0\"")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.alertTriggered", is(false)));

        mockMvc.perform(delete(BASE_URL + "/{id}", id).header("If-Match", "\"1\""))
                .andExpect(status().isNoContent());

        mockMvc.perform(get(BASE_URL + "/{id}", id))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturnBadRequestWhenPayloadIsInvalid() throws Exception {
        EnergyConsumptionRequest invalid = buildRequest(100, 200);
        invalid.setFacilityId("");

        mockMvc.perform(post(BASE_URL)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldListCreatedEnergyConsumptions() throws Exception {
        mockMvc.perform(post(BASE_URL)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(buildRequest(500, 3000))))
                .andExpect(status().isCreated());

        mockMvc.perform(get(BASE_URL).param("page", "0").param("size", "50"))
                .andExpect(status().isOk());
    }
}
