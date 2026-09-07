package com.ecocity.esg.adapter.in.web;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

abstract class AbstractOpenApiTest {
    @Autowired private MockMvc mvc;
    @Autowired private ObjectMapper mapper;

    @Test
    void documentsAllTwentyFiveOperationsWithTheirSuccessResponses() throws Exception {
        byte[] json = mvc.perform(get("/v3/api-docs")).andExpect(status().isOk())
                .andReturn().getResponse().getContentAsByteArray();
        JsonNode document = mapper.readTree(json);
        assertThat(document.path("info").path("title").asText()).isEqualTo("EcoCity ESG API");
        JsonNode paths = document.path("paths");
        for (String resource : List.of("energy-consumptions", "waste-collections", "carbon-emissions",
                "diversity-reports", "environmental-licenses")) {
            String base = "/api/v1/" + resource;
            for (String method : List.of("get", "post")) {
                assertThat(paths.path(base).path(method).path("summary").asText()).isNotBlank();
            }
            for (String method : List.of("get", "put", "delete")) {
                assertThat(paths.path(base + "/{id}").path(method).path("summary").asText()).isNotBlank();
            }
            assertThat(paths.path(base).path("post").path("responses").has("201")).isTrue();
            assertThat(paths.path(base + "/{id}").path("delete").path("responses").has("204")).isTrue();
        }
        assertThat(document.path("components").path("schemas").has("EnergyConsumptionRequest")).isTrue();
        assertThat(document.path("components").path("schemas").has("ApiErrorResponse")).isTrue();
    }

    @Test
    void servesSwaggerUiAndItsConfiguration() throws Exception {
        mvc.perform(get("/swagger-ui.html")).andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/swagger-ui/index.html"));
        mvc.perform(get("/swagger-ui/index.html")).andExpect(status().isOk());
        mvc.perform(get("/v3/api-docs/swagger-config")).andExpect(status().isOk())
                .andExpect(jsonPath("$.url").value("/v3/api-docs"));
    }
}
