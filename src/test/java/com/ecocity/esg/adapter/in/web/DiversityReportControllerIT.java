package com.ecocity.esg.adapter.in.web;

import com.ecocity.esg.adapter.in.web.dto.request.DiversityReportRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import com.ecocity.esg.support.MongoIntegrationTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class DiversityReportControllerIT extends MongoIntegrationTest {

    private static final String BASE_URL = "/api/v1/diversity-reports";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private DiversityReportRequest buildRequest(double womenPercentage) {
        return DiversityReportRequest.builder()
                .department("Tecnologia")
                .totalEmployees(40)
                .womenPercentage(womenPercentage)
                .blackAndMixedRacePercentage(30)
                .personsWithDisabilitiesPercentage(5)
                .lgbtqiaPercentage(6)
                .reportingMonth("2026-09")
                .diversityTrainingCompleted(true)
                .build();
    }

    @Test
    void shouldCreateDiversityReport() throws Exception {
        mockMvc.perform(post(BASE_URL)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(buildRequest(45))))
                .andExpect(status().isCreated());
    }

    @Test
    void shouldRejectPercentageOutOfRange() throws Exception {
        mockMvc.perform(post(BASE_URL)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(buildRequest(200))))
                .andExpect(status().isBadRequest());
    }
}
