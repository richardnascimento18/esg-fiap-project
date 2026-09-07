package com.ecocity.esg.application.usecase;

import com.ecocity.esg.application.port.out.EnergyConsumptionRepositoryPort;
import com.ecocity.esg.domain.exception.ResourceNotFoundException;
import com.ecocity.esg.domain.model.EnergyConsumption;
import com.ecocity.esg.domain.model.EnergySourceType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EnergyConsumptionServiceTest {

    @Mock
    private EnergyConsumptionRepositoryPort repositoryPort;

    private EnergyConsumptionService service;

    @BeforeEach
    void setUp() {
        service = new EnergyConsumptionService(repositoryPort);
    }

    private EnergyConsumption sample(double consumption, double threshold) {
        return EnergyConsumption.builder()
                .facilityId("FAC-001")
                .facilityName("Paco Municipal")
                .city("Sao Paulo")
                .sourceType(EnergySourceType.GRID)
                .consumptionKwh(consumption)
                .thresholdKwh(threshold)
                .readingTimestamp(Instant.now())
                .build();
    }

    @Nested
    @DisplayName("create")
    class CreateTests {

        @Test
        @DisplayName("deve marcar alertTriggered quando o consumo ultrapassa o limite")
        void shouldTriggerAlertWhenConsumptionExceedsThreshold() {
            EnergyConsumption input = sample(4000, 3000);
            when(repositoryPort.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

            EnergyConsumption result = service.create(input);

            assertThat(result.isAlertTriggered()).isTrue();
            verify(repositoryPort).save(result);
        }

        @Test
        @DisplayName("nao deve marcar alertTriggered quando o consumo esta dentro do limite")
        void shouldNotTriggerAlertWhenConsumptionIsWithinThreshold() {
            EnergyConsumption input = sample(1000, 3000);
            when(repositoryPort.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

            EnergyConsumption result = service.create(input);

            assertThat(result.isAlertTriggered()).isFalse();
        }

        @Test
        @DisplayName("deve ignorar qualquer id informado na criacao")
        void shouldIgnoreIncomingId() {
            EnergyConsumption input = sample(1000, 3000);
            input = input.toBuilder().id("should-be-ignored").build();
            when(repositoryPort.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

            EnergyConsumption result = service.create(input);

            assertThat(result.getId()).isNull();
            assertThat(input.getId()).isEqualTo("should-be-ignored");
        }
    }

    @Nested
    @DisplayName("update")
    class UpdateTests {

        @Test
        @DisplayName("deve atualizar os campos e recalcular o alerta")
        void shouldUpdateFieldsAndRecalculateAlert() {
            EnergyConsumption existing = sample(1000, 3000);
            existing = existing.toBuilder().id("abc123").build();
            when(repositoryPort.findById("abc123")).thenReturn(Optional.of(existing));
            when(repositoryPort.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

            EnergyConsumption update = sample(5000, 3000);
            EnergyConsumption result = service.update("abc123", update);

            assertThat(result.getConsumptionKwh()).isEqualTo(5000);
            assertThat(result.isAlertTriggered()).isTrue();
        }

        @Test
        @DisplayName("deve lancar ResourceNotFoundException quando o id nao existir")
        void shouldThrowWhenNotFound() {
            when(repositoryPort.findById("missing")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.update("missing", sample(100, 200)))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("delete")
    class DeleteTests {

        @Test
        @DisplayName("deve remover quando o registro existir")
        void shouldDeleteWhenExists() {
            when(repositoryPort.existsById("abc123")).thenReturn(true);

            service.delete("abc123");

            verify(repositoryPort, times(1)).deleteById("abc123");
        }

        @Test
        @DisplayName("deve lancar ResourceNotFoundException quando o registro nao existir")
        void shouldThrowWhenNotExists() {
            when(repositoryPort.existsById("missing")).thenReturn(false);

            assertThatThrownBy(() -> service.delete("missing"))
                    .isInstanceOf(ResourceNotFoundException.class);
            verify(repositoryPort, never()).deleteById(anyString());
        }
    }

    @Nested
    @DisplayName("findById")
    class FindByIdTests {

        @Test
        @DisplayName("deve retornar o registro quando encontrado")
        void shouldReturnWhenFound() {
            EnergyConsumption existing = sample(100, 200);
            when(repositoryPort.findById("abc123")).thenReturn(Optional.of(existing));

            EnergyConsumption result = service.findById("abc123");

            assertThat(result).isEqualTo(existing);
        }

        @Test
        @DisplayName("deve lancar ResourceNotFoundException quando nao encontrado")
        void shouldThrowWhenNotFound() {
            when(repositoryPort.findById("missing")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.findById("missing"))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("findAll")
    class FindAllTests {

        @Test
        @DisplayName("deve delegar paginacao para o repositorio")
        void shouldDelegatePagination() {
            List<EnergyConsumption> expected = List.of(sample(100, 200));
            when(repositoryPort.findAll(0, 20)).thenReturn(expected);

            List<EnergyConsumption> result = service.findAll(0, 20);

            assertThat(result).isEqualTo(expected);
        }
    }
}
