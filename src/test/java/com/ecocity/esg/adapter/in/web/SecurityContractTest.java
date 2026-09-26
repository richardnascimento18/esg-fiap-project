package com.ecocity.esg.adapter.in.web;

import com.ecocity.esg.support.WebContractTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;

@WebContractTest
class SecurityContractTest {
    @Autowired MockMvc mvc;

    @Test @WithAnonymousUser
    void publicReadsAndHealthRemainAvailable() throws Exception {
        mvc.perform(get("/api/v1/energy-consumptions")).andExpect(status().isOk());
        mvc.perform(get("/actuator/health/liveness")).andExpect(status().isOk());
    }

    @Test @WithAnonymousUser
    void writesAndManagementRequireAuthentication() throws Exception {
        mvc.perform(post("/api/v1/energy-consumptions")).andExpect(status().isUnauthorized());
        mvc.perform(delete("/api/v1/energy-consumptions/1")).andExpect(status().isUnauthorized());
        mvc.perform(get("/actuator/info")).andExpect(status().isUnauthorized());
    }

    @Test @WithMockUser(roles = "EDITOR")
    void editorCannotDeleteOrReadManagementInfo() throws Exception {
        mvc.perform(delete("/api/v1/energy-consumptions/1")).andExpect(status().isForbidden());
        mvc.perform(get("/actuator/info")).andExpect(status().isForbidden());
        mvc.perform(post("/api/v1/energy-consumptions")).andExpect(status().isBadRequest());
    }

    @Test @WithAnonymousUser
    void basicCredentialsAreCheckedWithoutLeakingDetails() throws Exception {
        mvc.perform(post("/api/v1/energy-consumptions").with(httpBasic("test-editor", "wrong-password")))
                .andExpect(status().isUnauthorized());
        mvc.perform(post("/api/v1/energy-consumptions").with(httpBasic("test-editor", "test-editor-password")))
                .andExpect(status().isBadRequest());
        mvc.perform(get("/actuator/info").with(httpBasic("test-admin", "test-admin-password")))
                .andExpect(status().isOk());
    }
}
