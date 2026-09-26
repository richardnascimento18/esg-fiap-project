package com.ecocity.esg.application.usecase;

import com.ecocity.esg.application.port.out.DiversityReportRepositoryPort;
import com.ecocity.esg.domain.exception.DomainValidationException;
import com.ecocity.esg.domain.exception.ResourceNotFoundException;
import com.ecocity.esg.domain.model.DiversityReport;
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
class DiversityReportServiceTest {

    @Mock
    private DiversityReportRepositoryPort repositoryPort;

    private DiversityReportService service;

    @BeforeEach
    void setUp() {
        service = new DiversityReportService(repositoryPort);
    }

    private DiversityReport sample(double womenPercentage) {
        return DiversityReport.builder()
                .department("Tecnologia")
                .totalEmployees(50)
                .womenPercentage(womenPercentage)
                .blackAndMixedRacePercentage(30)
                .personsWithDisabilitiesPercentage(5)
                .lgbtqiaPercentage(6)
                .reportingMonth("2026-09")
                .diversityTrainingCompleted(true)
                .build();
    }

    @Nested
    @DisplayName("create")
    class CreateTests {

        @Test
        @DisplayName("deve salvar quando os percentuais forem validos")
        void shouldSaveWhenValid() {
            when(repositoryPort.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

            DiversityReport result = service.create(sample(45));

            assertThat(result.getWomenPercentage()).isEqualTo(45);
        }

        @Test
        @DisplayName("deve rejeitar percentual acima de 100")
        void shouldRejectPercentageAboveHundred() {
            assertThatThrownBy(() -> service.create(sample(150)))
                    .isInstanceOf(DomainValidationException.class);
        }

        @Test
        @DisplayName("deve rejeitar percentual negativo")
        void shouldRejectNegativePercentage() {
            assertThatThrownBy(() -> service.create(sample(-1)))
                    .isInstanceOf(DomainValidationException.class);
        }
    }

    @Nested
    @DisplayName("update / delete / find")
    class OtherOperationsTests {

        @Test
        @DisplayName("update deve substituir os campos existentes")
        void shouldUpdateFields() {
            DiversityReport existing = sample(30);
            existing = existing.toBuilder().id("abc123").version(0L).build();
            when(repositoryPort.findById("abc123")).thenReturn(Optional.of(existing));
            when(repositoryPort.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

            DiversityReport result = service.update("abc123", sample(60), 0L);

            assertThat(result.getWomenPercentage()).isEqualTo(60);
        }

        @Test
        @DisplayName("delete deve lancar excecao quando nao existir")
        void shouldThrowOnDeleteWhenMissing() {
            when(repositoryPort.findById("missing")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.delete("missing", 0L)).isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        @DisplayName("findAll deve delegar para o repositorio")
        void shouldDelegateFindAll() {
            when(repositoryPort.findAll(0, 5)).thenReturn(java.util.List.of(sample(30)));

            assertThat(service.findAll(0, 5)).hasSize(1);
            verify(repositoryPort).findAll(0, 5);
        }
    }
}
