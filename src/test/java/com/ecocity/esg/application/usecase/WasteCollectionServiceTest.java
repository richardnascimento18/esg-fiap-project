package com.ecocity.esg.application.usecase;

import com.ecocity.esg.application.port.out.WasteCollectionRepositoryPort;
import com.ecocity.esg.domain.exception.DomainValidationException;
import com.ecocity.esg.domain.exception.ResourceNotFoundException;
import com.ecocity.esg.domain.model.WasteCollection;
import com.ecocity.esg.domain.model.WasteType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WasteCollectionServiceTest {

    @Mock
    private WasteCollectionRepositoryPort repositoryPort;

    private WasteCollectionService service;

    @BeforeEach
    void setUp() {
        service = new WasteCollectionService(repositoryPort);
    }

    private WasteCollection sample(double recyclingRate) {
        return WasteCollection.builder()
                .district("Centro")
                .wasteType(WasteType.RECYCLABLE)
                .weightKg(100)
                .recyclingRatePercentage(recyclingRate)
                .collectionDate(Instant.now())
                .collectorTeam("Equipe-01")
                .properlyDisposed(true)
                .build();
    }

    @Nested
    @DisplayName("create")
    class CreateTests {

        @Test
        @DisplayName("deve salvar quando a taxa de reciclagem for valida")
        void shouldSaveWhenValid() {
            when(repositoryPort.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

            WasteCollection result = service.create(sample(80));

            assertThat(result.getRecyclingRatePercentage()).isEqualTo(80);
        }

        @Test
        @DisplayName("deve rejeitar taxa de reciclagem fora do intervalo 0-100")
        void shouldRejectInvalidRate() {
            assertThatThrownBy(() -> service.create(sample(150)))
                    .isInstanceOf(DomainValidationException.class);
        }
    }

    @Nested
    @DisplayName("update")
    class UpdateTests {

        @Test
        @DisplayName("deve atualizar quando o registro existir")
        void shouldUpdateWhenExists() {
            WasteCollection existing = sample(50);
            existing = existing.toBuilder().id("abc123").version(0L).build();
            when(repositoryPort.findById("abc123")).thenReturn(Optional.of(existing));
            when(repositoryPort.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

            WasteCollection result = service.update("abc123", sample(90), 0L);

            assertThat(result.getRecyclingRatePercentage()).isEqualTo(90);
        }

        @Test
        @DisplayName("deve lancar ResourceNotFoundException quando nao existir")
        void shouldThrowWhenNotFound() {
            when(repositoryPort.findById("missing")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.update("missing", sample(50), 0L))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("delete")
    class DeleteTests {

        @Test
        @DisplayName("deve remover quando existir")
        void shouldDeleteWhenExists() {
            when(repositoryPort.findById("abc123")).thenReturn(Optional.of(sample(50).toBuilder().id("abc123").version(0L).build()));

            service.delete("abc123", 0L);

            verify(repositoryPort).delete(any());
        }

        @Test
        @DisplayName("nao deve remover quando nao existir")
        void shouldNotDeleteWhenMissing() {
            when(repositoryPort.findById("missing")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.delete("missing", 0L)).isInstanceOf(ResourceNotFoundException.class);
            verify(repositoryPort, never()).delete(any());
        }
    }

    @Nested
    @DisplayName("findById / findAll")
    class QueryTests {

        @Test
        @DisplayName("findById deve lancar excecao quando nao encontrado")
        void shouldThrowWhenNotFound() {
            when(repositoryPort.findById("missing")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.findById("missing")).isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        @DisplayName("findAll deve delegar para o repositorio")
        void shouldDelegateFindAll() {
            when(repositoryPort.findAll(1, 10)).thenReturn(java.util.List.of(sample(50)));

            assertThat(service.findAll(1, 10)).hasSize(1);
        }
    }
}
