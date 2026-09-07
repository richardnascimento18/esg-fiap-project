package com.ecocity.esg.application.usecase;

import com.ecocity.esg.application.port.out.EnvironmentalLicenseRepositoryPort;
import com.ecocity.esg.domain.exception.DomainValidationException;
import com.ecocity.esg.domain.exception.ResourceNotFoundException;
import com.ecocity.esg.domain.model.EnvironmentalLicense;
import com.ecocity.esg.domain.model.LicenseStatus;
import com.ecocity.esg.domain.model.LicenseType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.Clock;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EnvironmentalLicenseServiceTest {

    @Mock
    private EnvironmentalLicenseRepositoryPort repositoryPort;

    private EnvironmentalLicenseService service;

    @BeforeEach
    void setUp() {
        service = new EnvironmentalLicenseService(repositoryPort, Clock.systemUTC());
    }

    private EnvironmentalLicense sample(Instant issueDate, Instant expirationDate) {
        return EnvironmentalLicense.builder()
                .licenseNumber("LIC-2026-0001")
                .facility("Aterro Sanitario")
                .licenseType(LicenseType.OPERATION)
                .issueDate(issueDate)
                .expirationDate(expirationDate)
                .issuingAuthority("CETESB")
                .build();
    }

    @Nested
    @DisplayName("create")
    class CreateTests {

        @Test
        @DisplayName("deve marcar status ACTIVE quando ainda nao expirou")
        void shouldMarkActiveWhenNotExpired() {
            when(repositoryPort.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
            EnvironmentalLicense input = sample(Instant.now().minus(30, ChronoUnit.DAYS), Instant.now().plus(60, ChronoUnit.DAYS));

            EnvironmentalLicense result = service.create(input);

            assertThat(result.getStatus()).isEqualTo(LicenseStatus.ACTIVE);
        }

        @Test
        @DisplayName("deve marcar status EXPIRED quando a data de expiracao ja passou")
        void shouldMarkExpiredWhenPastDue() {
            when(repositoryPort.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
            EnvironmentalLicense input = sample(Instant.now().minus(400, ChronoUnit.DAYS), Instant.now().minus(10, ChronoUnit.DAYS));

            EnvironmentalLicense result = service.create(input);

            assertThat(result.getStatus()).isEqualTo(LicenseStatus.EXPIRED);
        }

        @Test
        @DisplayName("deve rejeitar quando a expiracao for anterior a emissao")
        void shouldRejectInvalidDateRange() {
            EnvironmentalLicense input = sample(Instant.now(), Instant.now().minus(1, ChronoUnit.DAYS));

            assertThatThrownBy(() -> service.create(input)).isInstanceOf(DomainValidationException.class);
        }
    }

    @Nested
    @DisplayName("update / delete / find")
    class OtherOperationsTests {

        @Test
        @DisplayName("update deve recalcular o status")
        void shouldRecalculateStatusOnUpdate() {
            EnvironmentalLicense existing = sample(Instant.now().minus(30, ChronoUnit.DAYS), Instant.now().plus(60, ChronoUnit.DAYS));
            existing = existing.toBuilder().id("abc123").build();
            when(repositoryPort.findById("abc123")).thenReturn(Optional.of(existing));
            when(repositoryPort.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

            EnvironmentalLicense update = sample(Instant.now().minus(400, ChronoUnit.DAYS), Instant.now().minus(5, ChronoUnit.DAYS));
            EnvironmentalLicense result = service.update("abc123", update);

            assertThat(result.getStatus()).isEqualTo(LicenseStatus.EXPIRED);
        }

        @Test
        @DisplayName("findById deve lancar excecao quando nao encontrado")
        void shouldThrowWhenNotFound() {
            when(repositoryPort.findById("missing")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.findById("missing")).isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        @DisplayName("delete deve lancar excecao quando nao existir")
        void shouldThrowOnDeleteWhenMissing() {
            when(repositoryPort.existsById("missing")).thenReturn(false);

            assertThatThrownBy(() -> service.delete("missing")).isInstanceOf(ResourceNotFoundException.class);
        }
    }
}
