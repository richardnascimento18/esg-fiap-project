package com.ecocity.esg.adapter.in.web.config;

import com.ecocity.esg.adapter.in.web.config.SecurityConfig;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SecurityConfigurationTest {
    private final SecurityConfig config = new SecurityConfig();

    @Test
    void rejectsExampleAndAmbiguousCredentialsAtStartup() {
        var encoder = config.passwordEncoder();
        assertThatThrownBy(() -> config.users("editor", "replace-with-local-password",
                "admin", "different-password", encoder)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> config.users("same", "editor-password",
                "same", "admin-password", encoder)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> config.users("editor", "same-password",
                "admin", "same-password", encoder)).isInstanceOf(IllegalArgumentException.class);
    }
}
