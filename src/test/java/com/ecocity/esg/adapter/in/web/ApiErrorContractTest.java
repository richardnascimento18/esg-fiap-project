package com.ecocity.esg.adapter.in.web;

import com.ecocity.esg.support.WebContractTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebContractTest
class ApiErrorContractTest {
    @Autowired private MockMvc mvc;

    @Test
    void invalidFieldsRetainThe400ErrorEnvelope() throws Exception {
        mvc.perform(post("/api/v1/energy-consumptions").contentType("application/json").content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Erro de validacao nos campos enviados"))
                .andExpect(jsonPath("$.path").value("/api/v1/energy-consumptions"))
                .andExpect(jsonPath("$.details").isArray())
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void dateValidationStillPrecedesLookupOnUpdate() throws Exception {
        String invalid = """
                {"licenseNumber":"INVALID","facility":"Parque","licenseType":"OPERATION",
                 "issueDate":"2026-02-01T00:00:00Z","expirationDate":"2026-01-01T00:00:00Z",
                 "issuingAuthority":"CETESB"}
                """;
        for (var request : new org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder[] {
                post("/api/v1/environmental-licenses"), put("/api/v1/environmental-licenses/missing")}) {
            mvc.perform(request.contentType("application/json").content(invalid))
                    .andExpect(status().isUnprocessableEntity())
                    .andExpect(jsonPath("$.status").value(422))
                    .andExpect(jsonPath("$.message").value("expirationDate nao pode ser anterior a issueDate"))
                    .andExpect(jsonPath("$.details").doesNotExist());
        }
    }

    @Test
    void omittedPrimitiveFieldsKeepTheirDefaultsAndNullMetadataIsOmitted() throws Exception {
        String payload = """
                {"facilityId":"defaults","facilityName":"Paço Municipal","city":"São Paulo",
                 "sourceType":"GRID","readingTimestamp":"2026-01-01T00:00:00Z"}
                """;
        mvc.perform(post("/api/v1/energy-consumptions").contentType("application/json").content(payload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.consumptionKwh").value(0.0))
                .andExpect(jsonPath("$.thresholdKwh").value(0.0))
                .andExpect(jsonPath("$.alertTriggered").value(false))
                .andExpect(jsonPath("$.sensorMetadata").doesNotExist());
    }

    @Test
    void malformedJsonRetainsTheExistingGenericErrorStatus() throws Exception {
        // Compatibility characterization: changing this to 400 is a separate API change.
        mvc.perform(post("/api/v1/energy-consumptions").contentType("application/json").content("{"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.startsWith("Erro interno inesperado: ")));
    }
}
