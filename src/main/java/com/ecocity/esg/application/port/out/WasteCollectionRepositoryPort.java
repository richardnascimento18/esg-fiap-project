package com.ecocity.esg.application.port.out;

import com.ecocity.esg.domain.model.WasteCollection;

import java.util.List;
import java.util.Optional;

public interface WasteCollectionRepositoryPort {

    WasteCollection save(WasteCollection wasteCollection);

    Optional<WasteCollection> findById(String id);

    List<WasteCollection> findAll(int page, int size);

    void deleteById(String id);

    void delete(WasteCollection wasteCollection);

    boolean existsById(String id);
}
