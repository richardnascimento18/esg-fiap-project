package com.ecocity.esg.application.port.in;

import com.ecocity.esg.domain.model.WasteCollection;

import java.util.List;

public interface WasteCollectionUseCase {

    WasteCollection create(WasteCollection wasteCollection);

    WasteCollection update(String id, WasteCollection wasteCollection);

    void delete(String id);

    WasteCollection findById(String id);

    List<WasteCollection> findAll(int page, int size);
}
