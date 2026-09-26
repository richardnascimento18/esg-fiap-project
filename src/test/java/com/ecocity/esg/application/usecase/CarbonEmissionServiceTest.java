package com.ecocity.esg.application.usecase;

import com.ecocity.esg.application.port.out.CarbonEmissionRepositoryPort;
import com.ecocity.esg.domain.exception.ResourceNotFoundException;
import com.ecocity.esg.domain.model.CarbonEmission;
import com.ecocity.esg.domain.model.EmissionType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CarbonEmissionServiceTest {

    @Mock
    private CarbonEmissionRepositoryPort repositoryPort;

    private CarbonEmissionService service;

    @BeforeEach
    void setUp() {
        service = new CarbonEmissionService(repositoryPort);
    }

    private CarbonEmission sample(double emission, double compensation) {
        return CarbonEmission.builder()
                .sourceFacility("Aterro Sanitario")
                .emissionType(EmissionType.CO2)
                .emissionTonnes(emission)
                .compensationTonnes(compensation)
                .reportingPeriod(com.ecocity.esg.domain.model.ReportingQuarter.parse("2026-Q3"))
                .auditedBy("Auditoria Ambiental Municipal")
                .build();
    }

    @Nested
    @DisplayName("create")
    class CreateTests {

        @Test
        @DisplayName("deve marcar compensated=true quando a compensacao cobre a emissao")
        void shouldMarkCompensatedWhenCoversEmission() {
            when(repositoryPort.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

            CarbonEmission result = service.create(sample(50, 60));

            assertThat(result.isCompensated()).isTrue();
        }

        @Test
        @DisplayName("deve marcar compensated=false quando a compensacao nao cobre a emissao")
        void shouldMarkNotCompensatedWhenBelowEmission() {
            when(repositoryPort.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

            CarbonEmission result = service.create(sample(50, 10));

            assertThat(result.isCompensated()).isFalse();
        }
    }

    @Nested
    @DisplayName("update / delete / find")
    class OtherOperationsTests {

        @Test
        @DisplayName("update deve recalcular o status de compensacao")
        void shouldRecalculateOnUpdate() {
            CarbonEmission existing = sample(50, 10);
            existing = existing.toBuilder().id("abc123").build();
            when(repositoryPort.findById("abc123")).thenReturn(Optional.of(existing));
            when(repositoryPort.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

            CarbonEmission result = service.update("abc123", sample(50, 100));

            assertThat(result.isCompensated()).isTrue();
        }

        @Test
        @DisplayName("delete deve lancar excecao quando nao existir")
        void shouldThrowOnDeleteWhenMissing() {
            when(repositoryPort.existsById("missing")).thenReturn(false);

            assertThatThrownBy(() -> service.delete("missing")).isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        @DisplayName("findById deve lancar excecao quando nao encontrado")
        void shouldThrowOnFindByIdWhenMissing() {
            when(repositoryPort.findById("missing")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.findById("missing")).isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        @DisplayName("findAll deve delegar para o repositorio")
        void shouldDelegateFindAll() {
            when(repositoryPort.findAll(0, 20)).thenReturn(java.util.List.of(sample(1, 1)));

            assertThat(service.findAll(0, 20)).hasSize(1);
            verify(repositoryPort).findAll(0, 20);
        }
    }
}
