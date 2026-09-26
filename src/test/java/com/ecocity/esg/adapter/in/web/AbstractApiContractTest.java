package com.ecocity.esg.adapter.in.web;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

abstract class AbstractApiContractTest {
    @Autowired private MockMvc mvc;
    @Autowired private ObjectMapper mapper;

    record Resource(String path, String payload, String derivedField, String expectedDerived) {}

    static Stream<Resource> resources() {
        return Stream.of(
                new Resource("energy-consumptions", """
                        {"facilityId":"contract","facilityName":"Paço Municipal","city":"São Paulo",
                         "sourceType":"SOLAR","consumptionKwh":3000.0,"thresholdKwh":3000.0,
                         "readingTimestamp":"2026-01-01T00:00:00Z",
                         "sensorMetadata":{"panelCount":40,"nested":{"values":[1,true,"á"]}}}
                        """, "alertTriggered", "false"),
                new Resource("waste-collections", """
                        {"district":"Centro","wasteType":"RECYCLABLE","weightKg":10.0,
                         "recyclingRatePercentage":100.0,"collectionDate":"2026-01-01T00:00:00Z",
                         "collectorTeam":"Equipe 1","properlyDisposed":true}
                        """, null, null),
                new Resource("carbon-emissions", """
                        {"sourceFacility":"Terminal","emissionType":"CO2","emissionTonnes":10.0,
                         "compensationTonnes":10.0,"reportingPeriod":"2026-Q1","auditedBy":"Equipe"}
                        """, "compensated", "true"),
                new Resource("diversity-reports", """
                        {"department":"Tecnologia","totalEmployees":30,"womenPercentage":50.0,
                         "blackAndMixedRacePercentage":40.0,"personsWithDisabilitiesPercentage":5.0,
                         "lgbtqiaPercentage":10.0,"reportingMonth":"2026-01","diversityTrainingCompleted":true}
                        """, null, null),
                new Resource("environmental-licenses", """
                        {"licenseNumber":"LIC-CONTRACT","facility":"Parque","licenseType":"OPERATION",
                         "issueDate":"2020-01-01T00:00:00Z","expirationDate":"2021-01-01T00:00:00Z",
                         "issuingAuthority":"CETESB","additionalRequirements":{"relatório":true}}
                        """, "status", "EXPIRED")
        );
    }

    @ParameterizedTest
    @MethodSource("resources")
    void preservesJsonFieldsStatusCodesPaginationAndDeletion(Resource resource) throws Exception {
        String base = "/api/v1/" + resource.path();
        JsonNode created = mapper.readTree(mvc.perform(post(base).contentType("application/json")
                        .content(resource.payload())).andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsByteArray());
        String id = created.path("id").asText();
        assertThat(id).isNotBlank();
        JsonNode request = mapper.readTree(resource.payload());
        request.fields().forEachRemaining(field -> assertThat(created.get(field.getKey())).isEqualTo(field.getValue()));
        assertThat(created.size()).isEqualTo(request.size() + 1 + (resource.derivedField() == null ? 0 : 1));
        if (resource.derivedField() != null) {
            assertThat(created.path(resource.derivedField()).asText()).isEqualTo(resource.expectedDerived());
        }
        mvc.perform(get(base + "/{id}", id)).andExpect(status().isOk())
                .andExpect(result -> assertThat(mapper.readTree(result.getResponse().getContentAsByteArray())).isEqualTo(created));
        mvc.perform(put(base + "/{id}", id).header("If-Match", "\"0\"").contentType("application/json").content(resource.payload()))
                .andExpect(status().isOk()).andExpect(result -> assertThat(mapper.readTree(result.getResponse().getContentAsByteArray())).isEqualTo(created));
        JsonNode firstPage = mapper.readTree(mvc.perform(get(base).param("page", "0").param("size", "1"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsByteArray());
        assertThat(firstPage.isArray()).isTrue();
        assertThat(firstPage.size()).isEqualTo(1);
        mvc.perform(delete(base + "/{id}", id).header("If-Match", "\"1\"")).andExpect(status().isNoContent()).andExpect(content().string(""));
        mvc.perform(get(base + "/{id}", id)).andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.path").value(base + "/" + id))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.details").doesNotExist());
        mvc.perform(delete(base + "/{id}", id).header("If-Match", "\"1\"")).andExpect(status().isNotFound());
    }
}
