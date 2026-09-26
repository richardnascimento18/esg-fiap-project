package com.ecocity.esg.adapter.in.web.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.config.http.SessionCreationPolicy;

@Configuration(proxyBeanMethods = false)
public class SecurityConfig {
    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http.csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/actuator/health", "/actuator/health/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/**", "/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/v1/**").hasAnyRole("EDITOR", "ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/v1/**").hasAnyRole("EDITOR", "ADMIN")
                        .anyRequest().hasRole("ADMIN"))
                .httpBasic(Customizer.withDefaults())
                .build();
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    UserDetailsService users(
            @Value("${app.security.editor.username}") String editorName,
            @Value("${app.security.editor.password}") String editorPassword,
            @Value("${app.security.admin.username}") String adminName,
            @Value("${app.security.admin.password}") String adminPassword,
            PasswordEncoder encoder) {
        if (editorName.isBlank() || adminName.isBlank() || editorPassword.length() < 12
                || adminPassword.length() < 12 || editorName.equals(adminName)
                || editorPassword.equals(adminPassword) || editorPassword.startsWith("replace-with-")
                || adminPassword.startsWith("replace-with-")) {
            throw new IllegalArgumentException("Security users require distinct names and non-example passwords of at least 12 characters");
        }
        return new InMemoryUserDetailsManager(
                User.withUsername(editorName).password(encoder.encode(editorPassword)).roles("EDITOR").build(),
                User.withUsername(adminName).password(encoder.encode(adminPassword)).roles("ADMIN").build());
    }
}
