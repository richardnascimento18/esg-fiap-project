package com.ecocity.esg.adapter.in.web;

import com.ecocity.esg.support.WebContractTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebContractTest
class ConditionalUpdateTest {
    @Autowired MockMvc mvc;
    private static final String URL = "/api/v1/waste-collections";
    private static final String BODY = """
            {"district":"Centro","wasteType":"RECYCLABLE","weightKg":10,
             "recyclingRatePercentage":50,"collectionDate":"2026-01-01T00:00:00Z",
             "collectorTeam":"Equipe","properlyDisposed":true}
            """;

    @Test
    void requiresStrongCurrentTagForUpdates() throws Exception {
        var created = mvc.perform(post(URL).contentType("application/json").content(BODY))
                .andExpect(status().isCreated()).andExpect(header().string("ETag", "\"0\""))
                .andReturn().getResponse();
        String id = new com.fasterxml.jackson.databind.ObjectMapper().readTree(created.getContentAsString()).path("id").asText();
        mvc.perform(put(URL + "/" + id).contentType("application/json").content(BODY))
                .andExpect(status().isPreconditionRequired());
        mvc.perform(put(URL + "/" + id).header("If-Match", "W/\"0\"").contentType("application/json").content(BODY))
                .andExpect(status().isBadRequest());
        mvc.perform(put(URL + "/" + id).header("If-Match", "\"0\"").contentType("application/json").content(BODY))
                .andExpect(status().isOk()).andExpect(header().string("ETag", "\"1\""));
        mvc.perform(put(URL + "/" + id).header("If-Match", "\"0\"").contentType("application/json").content(BODY))
                .andExpect(status().isPreconditionFailed());
        mvc.perform(delete(URL + "/" + id)).andExpect(status().isPreconditionRequired());
        mvc.perform(delete(URL + "/" + id).header("If-Match", "\"0\""))
                .andExpect(status().isPreconditionFailed());
        mvc.perform(delete(URL + "/" + id).header("If-Match", "\"1\""))
                .andExpect(status().isNoContent());
    }
}
