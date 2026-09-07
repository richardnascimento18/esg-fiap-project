package com.ecocity.esg.adapter.in.web;

import com.ecocity.esg.adapter.in.web.dto.request.CarbonEmissionRequest;
import com.ecocity.esg.domain.model.EmissionType;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import com.ecocity.esg.support.MongoIntegrationTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class CarbonEmissionControllerIT extends MongoIntegrationTest {

    private static final String BASE_URL = "/api/v1/carbon-emissions";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private CarbonEmissionRequest buildRequest(double emission, double compensation) {
        return CarbonEmissionRequest.builder()
                .sourceFacility("Aterro Sanitario")
                .emissionType(EmissionType.CO2)
                .emissionTonnes(emission)
                .compensationTonnes(compensation)
                .reportingPeriod("2026-Q3")
                .auditedBy("Auditoria Ambiental Municipal")
                .build();
    }

    @Test
    void shouldCreateAndRetrieveCompensatedEmission() throws Exception {
        String responseBody = mockMvc.perform(post(BASE_URL)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(buildRequest(40, 60))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.compensated", is(true)))
                .andReturn().getResponse().getContentAsString();

        String id = objectMapper.readTree(responseBody).get("id").asText();

        mockMvc.perform(get(BASE_URL + "/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sourceFacility", is("Aterro Sanitario")));
    }

    @Test
    void shouldReturnNotFoundForUnknownId() throws Exception {
        mockMvc.perform(get(BASE_URL + "/{id}", "non-existent-id"))
                .andExpect(status().isNotFound());
    }
}
