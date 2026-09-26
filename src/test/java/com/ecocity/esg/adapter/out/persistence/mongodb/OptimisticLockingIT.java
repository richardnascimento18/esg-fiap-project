package com.ecocity.esg.adapter.out.persistence.mongodb;

import com.ecocity.esg.application.port.out.WasteCollectionRepositoryPort;
import com.ecocity.esg.domain.model.WasteCollection;
import com.ecocity.esg.domain.model.WasteType;
import com.ecocity.esg.support.MongoIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.OptimisticLockingFailureException;

import java.time.Instant;

import static org.assertj.core.api.Assertions.*;

class OptimisticLockingIT extends MongoIntegrationTest {
    @Autowired WasteCollectionRepositoryPort repository;

    @Test
    void staleSnapshotCannotOverwriteNewerWrite() {
        WasteCollection original = repository.save(WasteCollection.builder()
                .district("Centro").wasteType(WasteType.RECYCLABLE).weightKg(10)
                .recyclingRatePercentage(50).collectionDate(Instant.parse("2026-01-01T00:00:00Z"))
                .collectorTeam("A").build());
        WasteCollection first = repository.findById(original.getId()).orElseThrow();
        WasteCollection stale = repository.findById(original.getId()).orElseThrow();
        WasteCollection updated = repository.save(first.toBuilder().weightKg(20).build());
        assertThat(updated.getVersion()).isEqualTo(original.getVersion() + 1);
        assertThatThrownBy(() -> repository.save(stale.toBuilder().weightKg(30).build()))
                .isInstanceOf(OptimisticLockingFailureException.class);
        assertThat(repository.findById(original.getId()).orElseThrow().getWeightKg()).isEqualTo(20);
    }
}
