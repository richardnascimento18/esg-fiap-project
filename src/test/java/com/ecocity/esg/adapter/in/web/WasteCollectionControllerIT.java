package com.ecocity.esg.adapter.in.web;

import com.ecocity.esg.adapter.in.web.dto.request.WasteCollectionRequest;
import com.ecocity.esg.domain.model.WasteType;
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

class WasteCollectionControllerIT extends MongoIntegrationTest {

    private static final String BASE_URL = "/api/v1/waste-collections";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private WasteCollectionRequest buildRequest(double recyclingRate) {
        return WasteCollectionRequest.builder()
                .district("Centro")
                .wasteType(WasteType.RECYCLABLE)
                .weightKg(120)
                .recyclingRatePercentage(recyclingRate)
                .collectionDate(Instant.now())
                .collectorTeam("Equipe-01")
                .properlyDisposed(true)
                .build();
    }

    @Test
    void shouldSupportFullCrudLifecycle() throws Exception {
        String responseBody = mockMvc.perform(post(BASE_URL)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(buildRequest(85))))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        String id = objectMapper.readTree(responseBody).get("id").asText();

        mockMvc.perform(get(BASE_URL + "/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.district", is("Centro")));

        mockMvc.perform(put(BASE_URL + "/{id}", id)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(buildRequest(40))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.recyclingRatePercentage", is(40.0)));

        mockMvc.perform(delete(BASE_URL + "/{id}", id))
                .andExpect(status().isNoContent());
    }

    @Test
    void shouldRejectRecyclingRateAboveOneHundred() throws Exception {
        mockMvc.perform(post(BASE_URL)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(buildRequest(150))))
                .andExpect(status().isBadRequest());
    }
}
